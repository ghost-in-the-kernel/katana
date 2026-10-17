package katana.intent.types;

/** What a fact says a thing means (docs/ROADMAP.md, phase 5.1), each kind about one shape of subject (INT-01). */
public enum Kind {
    MONEY(Subject.Datum.class), ENUM(Subject.Choice.class), FLAG(Subject.Datum.class), COUNTER(Subject.Datum.class), DATE(Subject.Datum.class),
    TAGGED_UNION(Subject.Union.class), FUNCTION(Subject.Range.class), REDUCIBLE(Subject.Paragraphs.class), STATELESS(Subject.Whole.class),
    NEVER_OVERFLOWS(Subject.Datum.class);

    private final Class<? extends Subject> shape;

    Kind(Class<? extends Subject> shape) {
        this.shape = shape;
    }

    /** The shape of subject a fact of this kind is about. */
    public Class<? extends Subject> shape() {
        return shape;
    }
}
