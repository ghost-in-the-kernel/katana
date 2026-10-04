import sys, os, glob, re, collections, ctypes
from tree_sitter import Language, Parser
lib = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'cobol.so')
cdll = ctypes.CDLL(lib)
fn = cdll.tree_sitter_COBOL
fn.restype = ctypes.c_void_p
lang = Language(fn())
parser = Parser(lang)

root = sys.argv[1]
files = sorted(p for p in glob.glob(os.path.join(root, '**', '*'), recursive=True)
               if re.search(r'\.(cbl|cob|cobol)$', p, re.I))
ok = bad = 0
kinds = collections.Counter()
for f in files:
    src = open(f, 'rb').read()
    tree = parser.parse(src)
    errs = []
    stack = [tree.root_node]
    while stack:
        n = stack.pop()
        if n.type == 'ERROR' or n.is_missing:
            errs.append(n)
        elif n.has_error:
            stack.extend(n.children)
    if not errs:
        ok += 1
        print('PASS', os.path.relpath(f, root))
    else:
        bad += 1
        e = min(errs, key=lambda n: n.start_point)
        line = src.splitlines()[e.start_point[0]].decode('latin-1') if e.start_point[0] < len(src.splitlines()) else ''
        print('FAIL', os.path.relpath(f, root), 'errors=%d' % len(errs), 'first@%d:%d' % (e.start_point[0] + 1, e.start_point[1]), '|', line.strip()[:90])
        kinds[re.sub(r'\d+', 'N', line.strip()[:40])] += 1
print('SUMMARY pass=%d fail=%d total=%d' % (ok, bad, len(files)))
