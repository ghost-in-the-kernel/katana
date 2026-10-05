package katana.sir.types;

import katana.front.Mapped;

/** SIR-01: the program, the file it was read from, and the profile it means what it means under, by hash (R-007, R-049). */
public record Header(String program, String source, String profileHash, Mapped.Origin origin) {
    public Header {
        if (!profileHash.matches("sha256:[0-9a-f]{64}")) throw new Invalid("SIR-01", "the profile hash is not sha256:<64 hex>: " + profileHash);
        Invalid.origin(origin, "the header");
    }
}
