package ollir;

import analysis.JmmSemanticsResult;

/**
 * This Stage deals with optimizations performed at the AST level and at the OLLIR level.
 * Note that for Checkpoint 2 (CP2) only the @{JmmOptimization#toOllir} has to be developed. The other two methods are
 * for Checkpoint 3 (CP3).
 */
public interface JmmOptimization {

    /**
     * Step 1 (for CP3): otimize code at the AST level
     *
     * @param semanticsResult
     * @return
     */
    default JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return semanticsResult;
    }

    /**
     * Step 2 (for CP2): convert the AST to the OLLIR format
     *
     * @param semanticsResult
     * @return
     */
    OllirResult toOllir(JmmSemanticsResult semanticsResult);

    /**
     * Step 3 (for CP3): otimize code at the OLLIR level
     *
     * @param semanticsResult
     * @return
     */
    default OllirResult optimize(OllirResult ollirResult) {
        return ollirResult;
    }
}