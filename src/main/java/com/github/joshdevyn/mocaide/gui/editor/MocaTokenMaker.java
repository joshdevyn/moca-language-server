package com.github.joshdevyn.mocaide.gui.editor;

import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.text.Segment;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom TokenMaker for MOCA language syntax highlighting
 */
public class MocaTokenMaker extends AbstractTokenMaker {
    
    private static final Map<String, Integer> keywords = new HashMap<>();
    
    static {
        // MOCA keywords
        String[] mocaKeywords = {
            "if", "elsif", "else", "endif", "while", "endwhile", "for", "endfor",
            "try", "catch", "endtry", "throw", "return", "break", "continue",
            "and", "or", "not", "in", "like", "is", "null", "true", "false",
            "create", "modify", "remove", "list", "count", "get", "set",
            "publish", "data", "where", "order", "by", "group", "having",
            "distinct", "all", "any", "some", "exists", "between", "case",
            "when", "then", "end", "union", "intersect", "except"
        };
        
        // MOCA built-in functions
        String[] mocaFunctions = {
            "nvl", "decode", "substr", "length", "upper", "lower", "trim",
            "to_char", "to_date", "to_number", "sysdate", "user", "rownum",
            "max", "min", "sum", "count", "avg", "stddev", "variance",
            "concat", "instr", "replace", "translate", "lpad", "rpad"
        };
        
        for (String keyword : mocaKeywords) {
            keywords.put(keyword, Token.RESERVED_WORD);
        }
        
        for (String function : mocaFunctions) {
            keywords.put(function, Token.FUNCTION);
        }
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();
        
        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;
        
        int newStartOffset = startOffset - offset;
        
        int currentTokenStart = offset;
        int currentTokenType = Token.NULL;
        
        for (int i = offset; i < end; i++) {
            char c = array[i];
            
            switch (currentTokenType) {
                case Token.NULL:
                    currentTokenStart = i;
                    
                    if (Character.isWhitespace(c)) {
                        currentTokenType = Token.WHITESPACE;
                    } else if (c == '-' && i + 1 < end && array[i + 1] == '-') {
                        currentTokenType = Token.COMMENT_EOL;
                    } else if (c == '/' && i + 1 < end && array[i + 1] == '*') {
                        currentTokenType = Token.COMMENT_MULTILINE;
                    } else if (c == '\'' || c == '"') {
                        currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                    } else if (Character.isDigit(c)) {
                        currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                    } else if (Character.isLetter(c) || c == '_') {
                        currentTokenType = Token.IDENTIFIER;
                    } else {
                        currentTokenType = Token.OPERATOR;
                    }
                    break;
                    
                case Token.WHITESPACE:
                    if (!Character.isWhitespace(c)) {
                        addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        if (c == '-' && i + 1 < end && array[i + 1] == '-') {
                            currentTokenType = Token.COMMENT_EOL;
                        } else if (c == '/' && i + 1 < end && array[i + 1] == '*') {
                            currentTokenType = Token.COMMENT_MULTILINE;
                        } else if (c == '\'' || c == '"') {
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                        } else if (Character.isDigit(c)) {
                            currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                        } else if (Character.isLetter(c) || c == '_') {
                            currentTokenType = Token.IDENTIFIER;
                        } else {
                            currentTokenType = Token.OPERATOR;
                        }
                    }
                    break;
                    
                case Token.IDENTIFIER:
                    if (!Character.isLetterOrDigit(c) && c != '_') {
                        String token = new String(array, currentTokenStart, i - currentTokenStart);
                        Integer keywordType = keywords.get(token.toLowerCase());
                        int tokenType = keywordType != null ? keywordType : Token.IDENTIFIER;
                        addToken(text, currentTokenStart, i - 1, tokenType, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        
                        if (Character.isWhitespace(c)) {
                            currentTokenType = Token.WHITESPACE;
                        } else if (c == '-' && i + 1 < end && array[i + 1] == '-') {
                            currentTokenType = Token.COMMENT_EOL;
                        } else if (c == '/' && i + 1 < end && array[i + 1] == '*') {
                            currentTokenType = Token.COMMENT_MULTILINE;
                        } else if (c == '\'' || c == '"') {
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                        } else if (Character.isDigit(c)) {
                            currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                        } else {
                            currentTokenType = Token.OPERATOR;
                        }
                    }
                    break;
                    
                case Token.LITERAL_NUMBER_DECIMAL_INT:
                    if (!Character.isDigit(c) && c != '.') {
                        addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        
                        if (Character.isWhitespace(c)) {
                            currentTokenType = Token.WHITESPACE;
                        } else if (Character.isLetter(c) || c == '_') {
                            currentTokenType = Token.IDENTIFIER;
                        } else {
                            currentTokenType = Token.OPERATOR;
                        }
                    }
                    break;
                    
                case Token.LITERAL_STRING_DOUBLE_QUOTE:
                    if (c == '\'' || c == '"') {
                        addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart);
                        currentTokenType = Token.NULL;
                    }
                    break;
                    
                case Token.COMMENT_EOL:
                    // EOL comments go to end of line
                    break;
                    
                case Token.COMMENT_MULTILINE:
                    if (c == '*' && i + 1 < end && array[i + 1] == '/') {
                        addToken(text, currentTokenStart, i + 1, Token.COMMENT_MULTILINE, newStartOffset + currentTokenStart);
                        currentTokenType = Token.NULL;
                        i++; // Skip the '/'
                    }
                    break;
                    
                case Token.OPERATOR:
                    if (Character.isWhitespace(c)) {
                        addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        currentTokenType = Token.WHITESPACE;
                    } else if (Character.isLetter(c) || c == '_') {
                        addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        currentTokenType = Token.IDENTIFIER;
                    } else if (Character.isDigit(c)) {
                        addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                        currentTokenStart = i;
                        currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                    }
                    break;
            }
        }
        
        // Add final token
        switch (currentTokenType) {
            case Token.NULL:
                addNullToken();
                break;
            case Token.IDENTIFIER:
                String token = new String(array, currentTokenStart, end - currentTokenStart);
                Integer keywordType = keywords.get(token.toLowerCase());
                int tokenType = keywordType != null ? keywordType : Token.IDENTIFIER;
                addToken(text, currentTokenStart, end - 1, tokenType, newStartOffset + currentTokenStart);
                addNullToken();
                break;
            default:
                addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
                addNullToken();
                break;
        }
        
        return firstToken;
    }

    @Override
    public String[] getLineCommentStartAndEnd(int languageIndex) {
        return new String[] { "--", null };
    }

    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        return type == Token.IDENTIFIER || type == Token.VARIABLE;
    }

    @Override
    public boolean isMarkupLanguage() {
        return false;
    }

    // This method seems to have signature issues with this version of RSyntaxTextArea
    // Our main tokenization is handled in getTokenList() above

    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap map = new TokenMap();
        
        // Add MOCA keywords
        String[] mocaKeywords = {
            "if", "elsif", "else", "endif", "while", "endwhile", "for", "endfor",
            "try", "catch", "endtry", "throw", "return", "break", "continue",
            "and", "or", "not", "in", "like", "is", "null", "true", "false",
            "create", "modify", "remove", "list", "count", "get", "set",
            "publish", "data", "where", "order", "by", "group", "having",
            "distinct", "all", "any", "some", "exists", "between", "case",
            "when", "then", "end", "union", "intersect", "except"
        };
        
        // Add MOCA functions
        String[] mocaFunctions = {
            "nvl", "decode", "substr", "length", "upper", "lower", "trim",
            "to_char", "to_date", "to_number", "sysdate", "user", "rownum",
            "max", "min", "sum", "count", "avg", "stddev", "variance",
            "concat", "instr", "replace", "translate", "lpad", "rpad"
        };
        
        for (String keyword : mocaKeywords) {
            map.put(keyword, Token.RESERVED_WORD);
        }
        
        for (String function : mocaFunctions) {
            map.put(function, Token.FUNCTION);
        }
        
        return map;
    }
}
