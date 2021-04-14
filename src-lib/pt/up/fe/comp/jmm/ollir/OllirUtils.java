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

package pt.up.fe.comp.jmm.ollir;

import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirAccesser;
import org.specs.comp.ollir.parser.OllirParser;
import org.specs.comp.ollir.parser.ParseException;

import pt.up.fe.specs.util.SpecsIo;

public class OllirUtils {

    /**
     * Parses OLLIR code.
     * 
     * @param code
     * @return a ClassUnit representing all the information from the OLLIR code
     * 
     */
    public static ClassUnit parse(String code) {
        OllirParser parser = new OllirParser(SpecsIo.toInputStream(code));

        try {
            // parse the input OLLIR and represent it by the class structure used
            parser.ClassUnit();

            // get Class instance representing the OLLIR file loaded
            return parser.getMyClass();
        } catch (ParseException e) {
            throw new RuntimeException("Exception while parsing OLLIR code", e);
        }

    }

    public static CallType getCallInvocationType(CallInstruction callInstruction) {
        return OllirAccesser.getCallInvocation(callInstruction);
    }

}
