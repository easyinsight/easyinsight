// $ANTLR 3.1.2 C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g 2010-09-05 12:27:51
 package com.easyinsight.calculations.generated; 

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CalculationsLexer extends Lexer {
    public static final int OpenBrace=14;
    public static final int CloseBrace=15;
    public static final int CloseParen=6;
    public static final int HideWhiteSpace=26;
    public static final int FuncEval=4;
    public static final int Subtract=8;
    public static final int Multiply=9;
    public static final int Exp=13;
    public static final int Decimal=18;
    public static final int Quote=16;
    public static final int BracketedVariable=22;
    public static final int Digit=27;
    public static final int EOF=-1;
    public static final int Add=7;
    public static final int Divide=10;
    public static final int VariableWhitespace=24;
    public static final int Variable=17;
    public static final int OpenParen=5;
    public static final int UpperCase=29;
    public static final int Character=30;
    public static final int Dot=12;
    public static final int LowerCase=28;
    public static final int String=19;
    public static final int NoBracketsVariable=23;
    public static final int UInteger=20;
    public static final int Whitespace=25;
    public static final int Comma=11;
    public static final int SpecialChars=31;
    public static final int Integer=21;
    public static final int NoBracketSpecialChars=32;

    // delegates
    // delegators

    public CalculationsLexer() {;} 
    public CalculationsLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CalculationsLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g"; }

    // $ANTLR start "OpenParen"
    public final void mOpenParen() throws RecognitionException {
        try {
            int _type = OpenParen;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:5:11: ( '(' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:5:13: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OpenParen"

    // $ANTLR start "CloseParen"
    public final void mCloseParen() throws RecognitionException {
        try {
            int _type = CloseParen;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:6:12: ( ')' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:6:14: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CloseParen"

    // $ANTLR start "Add"
    public final void mAdd() throws RecognitionException {
        try {
            int _type = Add;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:7:5: ( '+' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:7:7: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Add"

    // $ANTLR start "Subtract"
    public final void mSubtract() throws RecognitionException {
        try {
            int _type = Subtract;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:8:10: ( '-' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:8:12: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Subtract"

    // $ANTLR start "Multiply"
    public final void mMultiply() throws RecognitionException {
        try {
            int _type = Multiply;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:9:10: ( '*' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:9:12: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Multiply"

    // $ANTLR start "Divide"
    public final void mDivide() throws RecognitionException {
        try {
            int _type = Divide;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:10:8: ( '/' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:10:10: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Divide"

    // $ANTLR start "Comma"
    public final void mComma() throws RecognitionException {
        try {
            int _type = Comma;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:11:7: ( ',' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:11:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Comma"

    // $ANTLR start "Dot"
    public final void mDot() throws RecognitionException {
        try {
            int _type = Dot;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:12:5: ( '.' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:12:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Dot"

    // $ANTLR start "Exp"
    public final void mExp() throws RecognitionException {
        try {
            int _type = Exp;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:13:5: ( '^' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:13:7: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Exp"

    // $ANTLR start "OpenBrace"
    public final void mOpenBrace() throws RecognitionException {
        try {
            int _type = OpenBrace;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:14:11: ( '[' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:14:13: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OpenBrace"

    // $ANTLR start "CloseBrace"
    public final void mCloseBrace() throws RecognitionException {
        try {
            int _type = CloseBrace;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:15:12: ( ']' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:15:14: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CloseBrace"

    // $ANTLR start "Quote"
    public final void mQuote() throws RecognitionException {
        try {
            int _type = Quote;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:16:7: ( '\"' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:16:9: '\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Quote"

    // $ANTLR start "Decimal"
    public final void mDecimal() throws RecognitionException {
        try {
            int _type = Decimal;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:9: ( UInteger ( Dot UInteger )? ( 'E' Integer )? )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:11: UInteger ( Dot UInteger )? ( 'E' Integer )?
            {
            mUInteger(); 
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:20: ( Dot UInteger )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='.') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:21: Dot UInteger
                    {
                    mDot(); 
                    mUInteger(); 

                    }
                    break;

            }

            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:36: ( 'E' Integer )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='E') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:49:37: 'E' Integer
                    {
                    match('E'); 
                    mInteger(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Decimal"

    // $ANTLR start "Variable"
    public final void mVariable() throws RecognitionException {
        try {
            int _type = Variable;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:52:9: ( BracketedVariable | NoBracketsVariable )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='[') ) {
                alt3=1;
            }
            else if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||(LA3_0>='a' && LA3_0<='z')) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:52:11: BracketedVariable
                    {
                    mBracketedVariable(); 

                    }
                    break;
                case 2 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:52:31: NoBracketsVariable
                    {
                    mNoBracketsVariable(); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Variable"

    // $ANTLR start "String"
    public final void mString() throws RecognitionException {
        try {
            int _type = String;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:8: ( Quote ( VariableWhitespace )* ( Variable )? Quote )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:10: Quote ( VariableWhitespace )* ( Variable )? Quote
            {
            mQuote(); 
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:16: ( VariableWhitespace )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\t'||LA4_0==' ') ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:17: VariableWhitespace
            	    {
            	    mVariableWhitespace(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:38: ( Variable )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='[')||(LA5_0>='a' && LA5_0<='z')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:54:38: Variable
                    {
                    mVariable(); 

                    }
                    break;

            }

            mQuote(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "String"

    // $ANTLR start "HideWhiteSpace"
    public final void mHideWhiteSpace() throws RecognitionException {
        try {
            int _type = HideWhiteSpace;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:60:2: ( ( Whitespace )+ )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:60:4: ( Whitespace )+
            {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:60:4: ( Whitespace )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\t' && LA6_0<='\n')||(LA6_0>='\f' && LA6_0<='\r')||LA6_0==' ') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:60:4: Whitespace
            	    {
            	    mWhitespace(); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

              _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HideWhiteSpace"

    // $ANTLR start "Integer"
    public final void mInteger() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:62:18: ( ( Add | Subtract )? UInteger )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:62:20: ( Add | Subtract )? UInteger
            {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:62:20: ( Add | Subtract )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='+'||LA7_0=='-') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mUInteger(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "Integer"

    // $ANTLR start "Digit"
    public final void mDigit() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:65:2: ( '0' .. '9' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:65:4: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "Digit"

    // $ANTLR start "UInteger"
    public final void mUInteger() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:67:2: ( ( Digit )+ )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:67:4: ( Digit )+
            {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:67:4: ( Digit )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:67:4: Digit
            	    {
            	    mDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "UInteger"

    // $ANTLR start "VariableWhitespace"
    public final void mVariableWhitespace() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:70:2: ( ( '\\t' | ' ' ) )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:70:4: ( '\\t' | ' ' )
            {
            if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "VariableWhitespace"

    // $ANTLR start "Whitespace"
    public final void mWhitespace() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:73:2: ( ( VariableWhitespace | '\\r' | '\\n' | '\\u000C' ) )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:73:4: ( VariableWhitespace | '\\r' | '\\n' | '\\u000C' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Whitespace"

    // $ANTLR start "LowerCase"
    public final void mLowerCase() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:76:2: ( 'a' .. 'z' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:76:4: 'a' .. 'z'
            {
            matchRange('a','z'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "LowerCase"

    // $ANTLR start "UpperCase"
    public final void mUpperCase() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:78:2: ( 'A' .. 'Z' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:78:4: 'A' .. 'Z'
            {
            matchRange('A','Z'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "UpperCase"

    // $ANTLR start "Character"
    public final void mCharacter() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:81:2: ( LowerCase | UpperCase )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Character"

    // $ANTLR start "BracketedVariable"
    public final void mBracketedVariable() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:84:2: ( OpenBrace ( Character | Digit ) ( Character | Digit | SpecialChars | VariableWhitespace )* CloseBrace )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:84:4: OpenBrace ( Character | Digit ) ( Character | Digit | SpecialChars | VariableWhitespace )* CloseBrace
            {
            mOpenBrace(); 
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:84:34: ( Character | Digit | SpecialChars | VariableWhitespace )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='\t'||(LA9_0>=' ' && LA9_0<='Z')||LA9_0=='\\'||(LA9_0>='^' && LA9_0<='~')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:
            	    {
            	    if ( input.LA(1)=='\t'||(input.LA(1)>=' ' && input.LA(1)<='Z')||input.LA(1)=='\\'||(input.LA(1)>='^' && input.LA(1)<='~') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            mCloseBrace(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "BracketedVariable"

    // $ANTLR start "NoBracketsVariable"
    public final void mNoBracketsVariable() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:87:2: ( ( Character | Digit ) ( Character | Digit | VariableWhitespace )* )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:87:4: ( Character | Digit ) ( Character | Digit | VariableWhitespace )*
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:87:24: ( Character | Digit | VariableWhitespace )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='\t'||LA10_0==' '||(LA10_0>='0' && LA10_0<='9')||(LA10_0>='A' && LA10_0<='Z')||(LA10_0>='a' && LA10_0<='z')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' '||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NoBracketsVariable"

    // $ANTLR start "NoBracketSpecialChars"
    public final void mNoBracketSpecialChars() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:91:2: ( '_' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:91:4: '_'
            {
            match('_'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "NoBracketSpecialChars"

    // $ANTLR start "SpecialChars"
    public final void mSpecialChars() throws RecognitionException {
        try {
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:94:2: ( NoBracketSpecialChars | ':' | '<' | '>' | ',' | '.' | ';' | '/' | '?' | '\"' | '\\'' | '{' | '}' | '-' | '=' | '+' | '(' | ')' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '*' | '~' | '`' | '|' | '\\\\' )
            // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:
            {
            if ( (input.LA(1)>='!' && input.LA(1)<='/')||(input.LA(1)>=':' && input.LA(1)<='@')||input.LA(1)=='\\'||(input.LA(1)>='^' && input.LA(1)<='`')||(input.LA(1)>='{' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "SpecialChars"

    public void mTokens() throws RecognitionException {
        // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:8: ( OpenParen | CloseParen | Add | Subtract | Multiply | Divide | Comma | Dot | Exp | OpenBrace | CloseBrace | Quote | Decimal | Variable | String | HideWhiteSpace )
        int alt11=16;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:10: OpenParen
                {
                mOpenParen(); 

                }
                break;
            case 2 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:20: CloseParen
                {
                mCloseParen(); 

                }
                break;
            case 3 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:31: Add
                {
                mAdd(); 

                }
                break;
            case 4 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:35: Subtract
                {
                mSubtract(); 

                }
                break;
            case 5 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:44: Multiply
                {
                mMultiply(); 

                }
                break;
            case 6 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:53: Divide
                {
                mDivide(); 

                }
                break;
            case 7 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:60: Comma
                {
                mComma(); 

                }
                break;
            case 8 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:66: Dot
                {
                mDot(); 

                }
                break;
            case 9 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:70: Exp
                {
                mExp(); 

                }
                break;
            case 10 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:74: OpenBrace
                {
                mOpenBrace(); 

                }
                break;
            case 11 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:84: CloseBrace
                {
                mCloseBrace(); 

                }
                break;
            case 12 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:95: Quote
                {
                mQuote(); 

                }
                break;
            case 13 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:101: Decimal
                {
                mDecimal(); 

                }
                break;
            case 14 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:109: Variable
                {
                mVariable(); 

                }
                break;
            case 15 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:118: String
                {
                mString(); 

                }
                break;
            case 16 :
                // C:\\Users\\abaldwin\\Documents\\EasyInsight\\code\\java\\src\\com\\easyinsight\\calculations\\Calculations.g:1:125: HideWhiteSpace
                {
                mHideWhiteSpace(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\12\uffff\1\20\1\uffff\1\21\1\23\6\uffff\1\16\2\23";
    static final String DFA11_eofS =
        "\27\uffff";
    static final String DFA11_minS =
        "\1\11\11\uffff\1\60\1\uffff\2\11\6\uffff\1\53\2\11";
    static final String DFA11_maxS =
        "\1\172\11\uffff\1\172\1\uffff\2\172\6\uffff\1\71\2\172";
    static final String DFA11_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\13\2\uffff"+
        "\1\16\1\20\1\12\1\14\1\17\1\15\3\uffff";
    static final String DFA11_specialS =
        "\27\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\17\1\uffff\2\17\22\uffff\1\17\1\uffff\1\14\5\uffff\1\1\1"+
            "\2\1\5\1\3\1\7\1\4\1\10\1\6\12\15\7\uffff\32\16\1\12\1\uffff"+
            "\1\13\1\11\2\uffff\32\16",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\16\7\uffff\32\16\6\uffff\32\16",
            "",
            "\1\22\26\uffff\1\22\1\uffff\1\22\15\uffff\12\22\7\uffff\33"+
            "\22\5\uffff\32\22",
            "\1\16\26\uffff\1\16\17\uffff\12\25\7\uffff\4\16\1\24\25\16"+
            "\6\uffff\32\16",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\23\1\uffff\1\23\2\uffff\12\26",
            "\1\16\26\uffff\1\16\17\uffff\12\25\7\uffff\4\16\1\24\25\16"+
            "\6\uffff\32\16",
            "\1\16\26\uffff\1\16\17\uffff\12\26\7\uffff\32\16\6\uffff\32"+
            "\16"
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( OpenParen | CloseParen | Add | Subtract | Multiply | Divide | Comma | Dot | Exp | OpenBrace | CloseBrace | Quote | Decimal | Variable | String | HideWhiteSpace );";
        }
    }
 

}
