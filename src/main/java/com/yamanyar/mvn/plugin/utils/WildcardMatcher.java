package com.yamanyar.mvn.plugin.utils;

import org.apache.maven.plugin.logging.Log;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple wildcard matching utility class.
 * <p/>
 * The code is mostly copied from: http://www.rgagnon.com/javadetails/java-0515.html
 *
 * @author Kaan Yamanyar
 */
public class WildcardMatcher {

    private int ruleNo = 0;
    private static int ruleCounter = 0;
    private final Pattern pattern;
    private final Pattern methodPattern;
    private final String wildcardString;
    private final Log log;

    public WildcardMatcher(String wildcardString, Log log) {
        this.log = log;
        ruleNo = ++ruleCounter;
        if (wildcardString == null) throw new IllegalArgumentException("How can I match with null?");
        this.wildcardString = wildcardString;

        if (isMethod()) {
            this.pattern = Pattern.compile(wildcardToRegex(wildcardString.substring(0, wildcardString.lastIndexOf('.'))));
            this.methodPattern = Pattern.compile(wildcardToRegex(wildcardString));
        } else {
            this.pattern = Pattern.compile(wildcardToRegex(wildcardString));
            this.methodPattern = null;
        }
    }


    public boolean isMethod() {
        return wildcardString.endsWith("()");
    }

    private List<WildcardMatcher> exceptions;


    public boolean matchMethod(String testString) {
        return  matchAgainstPattern(testString,methodPattern);
    }

    public boolean match(String testString) {
        return matchAgainstPattern(testString, pattern);

    }

    private boolean matchAgainstPattern(String testString, Pattern patternToBeMatched) {
        boolean matchesPattern = patternToBeMatched.matcher(testString).matches();

        //is it included in exceptions:
        if (matchesPattern && exceptions != null) {
            for (WildcardMatcher exception : exceptions) {
                if (exception.match(testString)) {
                    log.debug(String.format("An exception to a restriction (%s of rule %d) matched %s.", wildcardString, ruleNo, testString));
                    return false;
                }
            }
        }

        return matchesPattern;
    }

    /**
     * @param wildcard
     * @return
     * @author: http://www.rgagnon.com/javadetails/java-0515.html
     */
    private String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WildcardMatcher that = (WildcardMatcher) o;

        if (!wildcardString.equals(that.wildcardString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return wildcardString.hashCode();
    }

    @Override
    public String toString() {
        return wildcardString;
    }

    public int getRuleNo() {
        return ruleNo;
    }

    public void setExceptions(List<WildcardMatcher> exceptions) {
        this.exceptions = exceptions;
    }

    public String getExceptions() {
        if (this.exceptions == null || exceptions.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        Iterator<WildcardMatcher> iterator = exceptions.iterator();
        while (iterator.hasNext()) {
            WildcardMatcher next =  iterator.next();
            sb.append(next.wildcardString);
            if (iterator.hasNext()) sb.append(",");

        }

        return sb.toString();
    }
}

