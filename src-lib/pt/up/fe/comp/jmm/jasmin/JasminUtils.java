package pt.up.fe.comp.jmm.jasmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import jas.jasError;
import jasmin.ClassFile;

/**
 * 
 * @author Tiago Carvalho
 *
 */
public class JasminUtils {
    /**
     * Extracted from Jasmin code
     */
    public static File assemble(File inputFile, File outputDir) {

        try (FileInputStream fs = new FileInputStream(inputFile);
                InputStreamReader ir = new InputStreamReader(fs);
                BufferedReader inp = new BufferedReader(ir);) {

            ClassFile classFile = new ClassFile();
            classFile.readJasmin(inp, inputFile.getName(), true);

            // if we got some errors, don't output a file - just return.
            if (classFile.errorCount() > 0) {
                throw new RuntimeException("Found "
                        + classFile.errorCount() + " errors while compiling Jasmin code.");

            }

            String class_path[] = (splitClassField(
                    classFile.getClassName()));
            String class_name = class_path[1];

            // determine where to place this class file
            // String dest_dir = dest_path;
            if (class_path[0] != null) {
                String class_dir = convertChars(
                        class_path[0], "./",
                        File.separatorChar);
                outputDir = new File(outputDir, class_dir);

            }
            // iocause = class_name + ".class: file can't be created";
            // if (dest_dir == null) {
            // out_file = new File(class_name + ".class");
            // } else {
            File out_file = new File(outputDir, class_name + ".class");

            // check that dest_dir exists

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            if (!outputDir.isDirectory()) {
                throw new IOException(
                        "Cannot create directory: " + outputDir.getAbsolutePath() + " is not a directory.");
            }

            try (FileOutputStream outp = new FileOutputStream(out_file);) {
                classFile.write(outp);
            }
            // System.out.println("Generated: " + out_file.getPath());
            return out_file;
        } catch (java.io.FileNotFoundException e) {
            throw new RuntimeException("Class could not be created: " + e.getMessage(), e);
        } catch (jasError e) {
            throw new RuntimeException("JAS Error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Exception while assembling Jasmin file", e);
        }

    }

    //
    // Splits a string like:
    // "java/lang/System/out"
    // into two strings:
    // "java/lang/System" and "out"
    //
    public static String[] splitClassField(String name) {
        String result[] = new String[2];
        int i, pos = -1, sigpos = 0;
        for (i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '.' || c == '/')
                pos = i;
        }
        if (pos == -1) { // no '/' in string
            result[0] = null;
            result[1] = name;
        } else {
            result[0] = convertChars(name.substring(0, pos), ".", '/'); // Maps '.' characters to '/' characters in a
                                                                        // string
            result[1] = name.substring(pos + 1);
        }

        return result;
    }

    //
    // Maps chars to toChar in a given String
    //
    public static String convertChars(String orig_name,
            String chars, char toChar) {
        StringBuffer tmp = new StringBuffer(orig_name);
        int i;
        for (i = 0; i < tmp.length(); i++) {
            if (chars.indexOf(tmp.charAt(i)) != -1) {
                tmp.setCharAt(i, toChar);
            }
        }
        return new String(tmp);
    }
}
