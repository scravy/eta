package eta.runtime;

import java.util.List;
import java.util.ArrayList;

import static eta.runtime.Rts.stgExit;
import static eta.runtime.Rts.ExitCode.EXIT_FAILURE;
import static eta.runtime.Rts.ExitCode.EXIT_SUCCESS;
import static eta.runtime.RtsFlags.RtsOptsEnabled.*;
import static eta.runtime.RuntimeLogging.errorBelch;

public class RuntimeOptions {
    public static String progName;
    public static String[] fullProgArgs;
    public static List<String> progArgs = new ArrayList<String>();
    public static List<String> rtsArgs = new ArrayList<String>();

    public static void setup(String[] args) {
        setFullProgArgs(args);
        int argc = args.length;
        int totalArgs = argc;
        setProgName(args);
        progArgs.clear();
        int rtsArgc = 0;
        if (rtsOpts != null) {
            splitRtsFlags(rtsOpts);
            procRtsOpts(rtsArgc);
            rtsArgc = rtsArgs.size();
        }
        String env = System.getenv("ETA_RTS");
        if (env != null) {
            splitRtsFlags(env);
            procRtsOpts(rtsArgc);
            rtsArgc = rtsArgs.size();
        }

        int i = 0;
        boolean rts = false;
        for (rts = false; i < argc; i++) {
            String arg = args[i];
            if (arg.equals("--RTS")) {
                i++;
                break;
            } else if (arg.equals("--")) {
                break;
            } else if (arg.equals("+RTS")) {
                rts = true;
            } else if (arg.equals("-RTS")) {
                rts = false;
            } else if (rts) {
                appendRtsArg(arg);
            } else {
                progArgs.add(arg);
            }
        }

        for(; i < argc; i++) {
            String arg = args[i];
            progArgs.add(arg);
        }

        procRtsOpts(rtsArgc);
        normaliseRtsOpts();
    }

    public static void splitRtsFlags(String s) {
        String[] args = s.trim().split("\\s+");
        for (String arg: args) {
            appendRtsArg(arg);
        }
    }

    public static void procRtsOpts(int rtsArgc) {
        if (rtsArgc >= rtsArgs.size()) return;
        boolean error = false;
        /* TODO: Check suid? */
        for (int i = rtsArgc; i < rtsArgs.size(); i++) {
            boolean optionChecked = false;
            String arg = rtsArgs.get(i);
            if (arg.charAt(0) != '-') {
                System.out.flush();
                errorBelch("unexpected RTS argument: %s", arg);
                error = true;
            } else {
                switch(arg.charAt(1)) {
                    case '?':
                        optionChecked = true;
                        error = true;
                        break;
                    case '-':
                        String option = arg.substring(2);
                        optionChecked = true;
                        if (option.equals("info")) {
                            printRtsInfo();
                            stgExit(EXIT_SUCCESS);
                        } else {
                            errorBelch("unknown RTS options: %s", arg);
                            error = true;
                        }
                        break;
                    case 'D':
                        optionChecked = true;
                        String rest = arg.substring(2);
                        for (char c: rest.toCharArray()) {
                            switch (c) {
                                case 's':
                                    RtsFlags.DebugFlags.scheduler = true;
                                    break;
                                default:
                                    badOption(arg);
                            }
                        }
                        break;
                    case 'C':
                        optionChecked = true;
                        checkUnsafe(isHsMain, rtsOptsEnabled);
                        if (arg.length() == 2) {
                            Runtime.setMinTSOIdleTime(0);
                        } else {
                            long ms = 0;;
                            try {
                                ms = (long)(1000 * Float.parseFloat(arg.substring(2)));
                            } catch (NumberFormatException e) {
                                errorBelch("bad value for -C");
                                error = true;
                            }
                            Runtime.setMinTSOIdleTime(ms);
                        }
                        break;
                    case 'N':
                        optionChecked = true;
                        if (arg.length() == 2) {
                            Runtime.setMaxWorkerCapabilities(1);
                        } else {
                            int nNodes = getIntOrZero(arg.substring(2));
                            if (nNodes <= 0) {
                                errorBelch("bad value for -N");
                                error = true;
                            }
                            Runtime.setMaxWorkerCapabilities(nNodes);
                        }
                        break;
                    case 'e':
                        optionChecked = true;
                        if (arg.length() > 2) {
                            int maxGlobalSparks = getIntOrZero(arg.substring(2));
                            if (maxGlobalSparks <= 0) {
                                errorBelch("bad value for -e");
                                error = true;
                            }
                            Runtime.setMaxGlobalSparks(maxGlobalSparks);
                        }
                        break;
                    default:
                        optionChecked = true;
                        errorBelch("unknown RTS option: %s", arg);
                        error = true;
                        break;
                }
                if (!optionChecked) {
                    errorBelch("Internal error in the RTS options parser");
                    stgExit(EXIT_FAILURE);
                }
            }
        }
        if (error) errorUsage();
    }

    public static void errorUsage() {
        System.out.flush();
        /* TODO: Usage text */
        System.out.println("Wrong usage. Consult the source code.");
        stgExit(EXIT_FAILURE);
    }

    public static void normaliseRtsOpts() {}

    public static int getIntOrZero(String s) {
        int res;
        try {
            res = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            res = 0;
        }
        return res;
    }

    public static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void badOption(String s) {
        errorBelch("bad RTS option: %s", s);
        stgExit(EXIT_FAILURE);
    }

    public static void appendRtsArg(String arg) {
        rtsArgs.add(arg);
    }

    public static void setProgName(String[] args) {
        /* TODO: Implement setting program name */
    }

    public static void setFullProgArgs(String[] args) {
        fullProgArgs = args;
    }

    public static void printRtsInfo() {
        /* TODO: Implement */
    }
}