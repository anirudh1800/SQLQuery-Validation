import java.util.*;

public class Main {

    static final List<String> keywords = Arrays.asList(new String[]{"CREATE",
            "TABLE", "INSERT", "SELECT", "INTO", "FROM", "WHERE", "*", "(", ")",
            "MDSYS.SPATIAL_INDEX", "INDEXTYPE", "PRIMARY", "KEY",
            "REFERENCES", "NULL", "NOT", "LIKE", "IN", "BETWEEN", "OR", "AND", "VALUES",
            "=", ">", "<", "<=", ">="});

    static final List<String> dataTypes = Arrays.asList(new String[]{"CHAR", "VARCHAR", "VARCHAR2", "TINYTEXT", "TEXT", "BLOB", "MEDIUMTEXT",
            "MEDIUMBLOB", "LONGTEXT", "LONGBLOB", "ENUM", "SET", "NUMBER",
            "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "BIGINT", "FLOAT", "DOUBLE",
            "DECIMAL", "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR", "SDO_ELEM_INFO_ARRAY", "SDO_ORDINATE_ARRAY", "SDO_GEOMETRY"});

    static final HashSet<String> keywordSet = new HashSet<>(keywords);
    static final HashSet<String> dataTypeSet = new HashSet<>(dataTypes);
    static HashMap<String, String> variableMaps = new HashMap<>();

    public static State currentState = State.INITIAL;
    public static String input;
    static int pos = 0;

    public enum State {

        INITIAL {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                switch (word.toUpperCase()) {

                    case "CREATE":
                        return State.CREATE;

                    case "INSERT":
                        return State.INSERT;

                    case "SELECT":
                        return State.SELECT;

                    case "":
                        return State.INITIAL;

                    default:
                        throw new ParseException("Syntax error in query:" + (pos + 1));
                }
            }
        },

        CREATE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (word.equalsIgnoreCase("TABLE") && event == ' ') {
                    return State.TABLE;
                } else if (word.isEmpty()) {
                    return State.CREATE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        INSERT {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (word.equalsIgnoreCase("INTO") && event == ' ') {
                    return State.INTO;
                } else if (word.isEmpty()) {
                    return State.INSERT;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        INTO {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (isName(word)) {
                    if (event == ' ') {
                        return State.NAME;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.isEmpty()) {
                    return State.INTO;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        SELECT {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                return State.SELECT;
            }
        },

        TABLE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (isName(word)) {
                    if (event == ' ') {
                        if (variableMaps.containsValue(word))
                            throw new ParseException("Table already exists:" + (pos + 1));
                        else variableMaps.put("TABLE_NAME", word);
                        return State.NAME;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.isEmpty()) {
                    return State.TABLE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        NAME {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == '(' && isName(word)) {
                    variableMaps.put("VARIABLE_NAME", word);
                    return State.NAME;
                } else if (event == ' ' && dataTypeSet.contains(word)) {
                    return State.ATTRIBUTE_TYPE;
                } else if (word.isEmpty()) {
                    return State.NAME;
                } else if (word.equalsIgnoreCase("VALUES")) {
                    return State.VALUES;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        KEYWORD {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && keywordSet.contains(word)) {
                    return State.KEYWORD;
                } else if (event == ',' && isName(word)) {
                    return State.NAME;
                } else if (event == ')') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.KEYWORD;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        ATTRIBUTE_TYPE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && keywordSet.contains(word)) {
                    return State.KEYWORD;
                } else if (event == ',' && isName(word)) {
                    return State.NAME;
                } else if (event == ')') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.ATTRIBUTE_TYPE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        VALUES {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == '(' && isValue(word)) {
                    return State.VALUE;
                } else if (word.isEmpty()) {
                    return State.VALUES;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        VALUE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ',' && isValue(word)) {
                    return State.VALUE;
                } else if (event == ')') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.VALUE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        END {
            @Override
            public State nextState(String word, Character event) {
                return State.END;
            }
        };

        public abstract State nextState(String word, Character event) throws ParseException;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String word = new String();
        Character event = null;

        input = scanner.nextLine();
        pos = 0;

        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            word += input.charAt(pos++);
        }

        try {
            currentState = currentState.nextState(word, event);

            while (currentState != State.END && pos < input.length()) {
                String next_word = new String();

                if (!word.isEmpty())
                    event = input.charAt(pos++);
                else if (word.isEmpty() && event == ' ') {
                    event = input.charAt(pos++);
                } else pos++;

                switch (currentState) {

                    case VALUES:
                    case VALUE:
                        boolean flag = false;
                        while (pos < input.length()) {
                            switch (input.charAt(pos)) {
                                case ' ':
                                    flag = true;
                                    break;

                                case ',':
                                    flag = true;
                                    break;

                                case ')':
                                    flag = true;
                                    break;

                                case '(':
                                    flag = true;
                                    break;

                                default:
                                    next_word += input.charAt(pos++);
                                    break;
                            }
                            if (flag)
                                break;
                        }
                        break;

                    default:
                        while (pos < input.length() && ((input.charAt(pos) == '_' && next_word.length() > 1) || Character.isLetter(input.charAt(pos)) || (Character.isDigit(input.charAt(pos)) && next_word.length() > 1))) {
                            next_word += input.charAt(pos++);
                        }
                        break;
                }
                word = next_word;
                currentState = currentState.nextState(word, event);
            }

            if (currentState == State.END) {
                System.out.println("Successfully parsed query:" + (pos + 1));
            } else throw new ParseException("Syntax error in query:" + (pos + 1));
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static boolean isName(String word) throws ParseException {
        if (word.length() > 0 && !keywordSet.contains(word.toUpperCase()) && !dataTypeSet.contains(word.toUpperCase())) {
            if (variableMaps.containsValue(word))
                throw new ParseException("Variable " + word + " already used:" + (pos + 1));
            else
                return true;
        } else return false;
    }

    public static boolean isValue(String word) throws ParseException {
        if (word.length() > 0) {
            if (isInteger(word) || isText(word) || isFloat(word))
                return true;
            else if (isSdogeometry(word)) {
                int tmp = pos;
                if (input.charAt(pos) == '(') {
                    while (input.charAt(pos) != ')' && pos < input.length()) {
                        pos++;
                    }
                    if (pos == input.length())
                        throw new ParseException("SDO_GEOMETRY type error:" + (tmp + 1));
                    pos++;
                    return true;
                } else throw new ParseException("SDO_GEOMETRY type error:" + (tmp + 1));

            } else {
                return false;
            }
        }
        return false;
    }

    private static boolean isText(String word) {
        if (word.startsWith("'") && word.endsWith("'"))
            return true;
        else
            return false;
    }

    public static boolean isSdogeometry(String word) {
        if (word.startsWith("SDO_GEOMETRY")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFloat(String word) {
        boolean isValidFloat = false;
        try {
            Float.parseFloat(word);
            isValidFloat = true;
        } catch (NumberFormatException ex) {
        }

        return isValidFloat;
    }

    public static boolean isInteger(String word) {
        boolean isValidInteger = false;
        try {
            Integer.parseInt(word);
            isValidInteger = true;
        } catch (NumberFormatException ex) {
        }

        return isValidInteger;
    }
}

class ParseException extends Exception {
    public ParseException(String s) {
        super(s);
    }
}
