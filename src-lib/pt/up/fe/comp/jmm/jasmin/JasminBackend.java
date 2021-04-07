package pt.up.fe.comp.jmm.jasmin;

import pt.up.fe.comp.jmm.ollir.OllirResult;

/**
 * This Stage converts the OLLIR to Jasmin Bytecodes with optimizations performed at the AST level and at the OLLIR
 * level.<br>
 * Note that this step also for Checkpoint 2 (CP2), but only for code structures defined in the project description.
 */
public interface JasminBackend {

    /**
     * * Converts the OLLIR to Jasmin Bytecodes with optimizations performed at the AST level and at the OLLIR
     * level.<br>
     * Note that this step also for Checkpoint 2 (CP2), but only for code structures defined in the project description.
     * 
     * @param ollirResult
     * @return
     */
    JasminResult toJasmin(OllirResult ollirResult);

}