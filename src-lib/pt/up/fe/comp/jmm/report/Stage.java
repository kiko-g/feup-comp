package pt.up.fe.comp.jmm.report;

public enum Stage {
    LEXICAL,
    SYNTATIC,
    SEMANTIC,
    LLIR,
    OPTIMIZATION,
    GENERATION,
    OTHER // e.g. while parsing inputs, uncaught error, etc.
}
