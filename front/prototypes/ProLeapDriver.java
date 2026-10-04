import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import io.proleap.cobol.asg.metamodel.Program;
import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.params.CobolDialect;
import io.proleap.cobol.asg.params.impl.CobolParserParamsImpl;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

/** Runs the ProLeap pipeline (preprocessor + ANTLR parser + ASG) over every program under a root.
 *  Usage: Driver <root> <copybookDir>... ; reports per-file PASS/FAIL with the first error. */
public class Driver {
  public static void main(String[] a) throws Exception {
    Path root = Paths.get(a[0]);
    List<File> cpyDirs = new ArrayList<>();
    for (int i = 1; i < a.length; i++) cpyDirs.add(new File(a[i]));
    List<Path> files;
    try (Stream<Path> s = Files.walk(root)) {
      files = s.filter(p -> { String n = p.toString().toLowerCase(); return n.endsWith(".cbl") || n.endsWith(".cob") || n.endsWith(".cobol"); }).sorted().collect(Collectors.toList());
    }
    int pass = 0, fail = 0;
    Map<String,Integer> errKinds = new TreeMap<>();
    for (Path f : files) {
      String best = null; boolean ok = false;
      for (CobolSourceFormatEnum fmt : new CobolSourceFormatEnum[]{CobolSourceFormatEnum.FIXED, CobolSourceFormatEnum.VARIABLE, CobolSourceFormatEnum.TANDEM}) {
        CobolParserParams p = new CobolParserParamsImpl();
        p.setFormat(fmt);
        p.setDialect(CobolDialect.MF);
        p.setCopyBookDirectories(cpyDirs);
        p.setCopyBookExtensions(Arrays.asList("cpy","CPY","cbl","CBL","cob","COB", ""));
        long t0 = System.currentTimeMillis();
        try {
          Program prog = new CobolParserRunnerImpl().analyzeFile(f.toFile(), p);
          int units = prog.getCompilationUnits().size();
          System.out.println("PASS\t" + fmt + "\t" + (System.currentTimeMillis()-t0) + "ms\tunits=" + units + "\t" + root.relativize(f));
          ok = true; break;
        } catch (Throwable e) {
          String m = String.valueOf(e.getMessage()).replace('\n',' ');
          if (m.length() > 300) m = m.substring(0,300);
          String cur = fmt + ": " + e.getClass().getSimpleName() + ": " + m;
          if (best == null || fmt == CobolSourceFormatEnum.FIXED) best = cur;
        }
      }
      if (ok) pass++; else {
        fail++;
        System.out.println("FAIL\t" + root.relativize(f) + "\t" + best);
        String k = best.replaceAll("line \\d+:\\d+", "").replaceAll("'[^']*'", "'x'").replaceAll("\\d+", "N");
        errKinds.merge(k.length()>160?k.substring(0,160):k, 1, Integer::sum);
      }
    }
    System.out.println("SUMMARY\tpass=" + pass + "\tfail=" + fail + "\ttotal=" + files.size());
    errKinds.entrySet().stream().sorted((x,y)->y.getValue()-x.getValue()).limit(25).forEach(e -> System.out.println("KIND\t" + e.getValue() + "\t" + e.getKey()));
  }
}
