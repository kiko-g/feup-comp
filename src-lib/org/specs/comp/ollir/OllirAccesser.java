/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.comp.ollir;

import java.util.HashMap;

public class OllirAccesser {

    /**
     * @deprecated Use {@link org.specs.comp.ollir.CallInstruction#getInvocationType()} instead.
     * @param callInstruction
     * @return
     */
    @Deprecated
    public static CallType getCallInvocation(CallInstruction callInstruction) {
        return callInstruction.invocationType;
    }

    /**
     * @deprecated Use {@link org.specs.comp.ollir.Operand#isParameter()} instead.
     * @param operand
     * @return
     */
    @Deprecated
    public static boolean isParameter(Operand operand) {
        return operand.isParameter;
    }

    /**
     * @deprecated Use {@link org.specs.comp.ollir.Method#getVarTable()} instead.
     * @param method
     * @return
     */
    @Deprecated
    public static HashMap<String, Descriptor> getVarTable(Method method) {
        return method.varTable;
    }

    /**
     * @deprecated Use {@link org.specs.comp.ollir.Method#getLabels()} instead.
     * @param method
     * @return
     */
    @Deprecated
    public static HashMap<String, Instruction> getLabels(Method method) {
        return method.methodLabels;
    }

}
