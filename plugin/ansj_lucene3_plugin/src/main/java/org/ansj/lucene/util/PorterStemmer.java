package org.ansj.lucene.util;

//import org.apache.lucene.util.ArrayUtil;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_CHAR;
//
///**
// * 抄袭lucene的英文处理
// *  Stemmer, implementing the Porter Stemming Algorithm
// *
// * The Stemmer class transforms a word into its root form. The input word can be
// * provided a character at time (by calling add()), or at once by calling one of
// * the various stem(something) methods.
// */
//
//public class PorterStemmer {
//
//	private char[] b;
//	private int i, /* offset into b */
//	j, k, k0;
//	private boolean dirty = false;
//	private static final int INITIAL_SIZE = 50;
//
//	public PorterStemmer() {
//		b = new char[INITIAL_SIZE];
//		i = 0;
//	}
//
//	/**
//	 * reset() resets the stemmer so it can stem another word. If you invoke the
//	 * stemmer by calling add(char) and then stem(), you must call reset()
//	 * before starting another word.
//	 */
//	public void reset() {
//		i = 0;
//		dirty = false;
//	}
//
//	/**
//	 * Add a character to the word being stemmed. When you are finished adding
//	 * characters, you can call stem(void) to process the word.
//	 */
//	public void add(char ch) {
//		if (b.length <= i) {
//			b = ArrayUtil.grow(b, i + 1);
//		}
//		b[i++] = ch;
//	}
//
//	/**
//	 * After a word has been stemmed, it can be retrieved by toString(), or a
//	 * reference to the internal buffer can be retrieved by getResultBuffer and
//	 * getResultLength (which is generally more efficient.)
//	 */
//	@Override
//	public String toString() {
//		return new String(b, 0, i);
//	}
//
//	/**
//	 * Returns the length of the word resulting from the stemming process.
//	 */
//	public int getResultLength() {
//		return i;
//	}
//
//	/**
//	 * Returns a reference to a character buffer containing the results of the
//	 * stemming process. You also need to consult getResultLength() to determine
//	 * the length of the result.
//	 */
//	public char[] getResultBuffer() {
//		return b;
//	}
//
//	/* cons(i) is true <=> b[i] is a consonant. */
//
//	private final boolean cons(int i) {
//		switch (b[i]) {
//		case 'a':
//		case 'e':
//		case 'i':
//		case 'o':
//		case 'u':
//			return false;
//		case 'y':
//			return (i == k0) ? true : !cons(i - 1);
//		default:
//			return true;
//		}
//	}
//
//	/*
//	 * m() measures the number of consonant sequences between k0 and j. if c is
//	 * a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
//	 * presence,
//	 *
//	 * <c><v> gives 0 <c>vc<v> gives 1 <c>vcvc<v> gives 2 <c>vcvcvc<v> gives 3
//	 * ....
//	 */
//
//	private final int m() {
//		int n = 0;
//		int i = k0;
//		while (true) {
//			if (i > j)
//				return n;
//			if (!cons(i))
//				break;
//			i++;
//		}
//		i++;
//		while (true) {
//			while (true) {
//				if (i > j)
//					return n;
//				if (cons(i))
//					break;
//				i++;
//			}
//			i++;
//			n++;
//			while (true) {
//				if (i > j)
//					return n;
//				if (!cons(i))
//					break;
//				i++;
//			}
//			i++;
//		}
//	}
//
//	/* vowelinstem() is true <=> k0,...j contains a vowel */
//
//	private final boolean vowelinstem() {
//		int i;
//		for (i = k0; i <= j; i++)
//			if (!cons(i))
//				return true;
//		return false;
//	}
//
//	/* doublec(j) is true <=> j,(j-1) contain a double consonant. */
//
//	private final boolean doublec(int j) {
//		if (j < k0 + 1)
//			return false;
//		if (b[j] != b[j - 1])
//			return false;
//		return cons(j);
//	}
//
//	/*
//	 * cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
//	 * and also if the second c is not w,x or y. this is used when trying to
//	 * restore an e at the end of a short word. e.g.
//	 *
//	 * cav(e), lov(e), hop(e), crim(e), but snow, box, tray.
//	 */
//
//	private final boolean cvc(int i) {
//		if (i < k0 + 2 || !cons(i) || cons(i - 1) || !cons(i - 2))
//			return false;
//		else {
//			int ch = b[i];
//			if (ch == 'w' || ch == 'x' || ch == 'y')
//				return false;
//		}
//		return true;
//	}
//
//	private final boolean ends(String s) {
//		int l = s.length();
//		int o = k - l + 1;
//		if (o < k0)
//			return false;
//		for (int i = 0; i < l; i++)
//			if (b[o + i] != s.charAt(i))
//				return false;
//		j = k - l;
//		return true;
//	}
//
//	/*
//	 * setto(s) sets (j+1),...k to the characters in the string s, readjusting
//	 * k.
//	 */
//
//	void setto(String s) {
//		int l = s.length();
//		int o = j + 1;
//		for (int i = 0; i < l; i++)
//			b[o + i] = s.charAt(i);
//		k = j + l;
//		dirty = true;
//	}
//
//	/* r(s) is used further down. */
//
//	void r(String s) {
//		if (m() > 0)
//			setto(s);
//	}
//
//	/*
//	 * step1() gets rid of plurals and -ed or -ing. e.g.
//	 *
//	 * caresses -> caress ponies -> poni ties -> ti caress -> caress cats -> cat
//	 *
//	 * feed -> feed agreed -> agree disabled -> disable
//	 *
//	 * matting -> mat mating -> mate meeting -> meet milling -> mill messing ->
//	 * mess
//	 *
//	 * meetings -> meet
//	 */
//
//	private final void step1() {
//		if (b[k] == 's') {
//			if (ends("sses"))
//				k -= 2;
//			else if (ends("ies"))
//				setto("i");
//			else if (b[k - 1] != 's')
//				k--;
//		}
//		if (ends("eed")) {
//			if (m() > 0)
//				k--;
//		} else if ((ends("ed") || ends("ing")) && vowelinstem()) {
//			k = j;
//			if (ends("at"))
//				setto("ate");
//			else if (ends("bl"))
//				setto("ble");
//			else if (ends("iz"))
//				setto("ize");
//			else if (doublec(k)) {
//				int ch = b[k--];
//				if (ch == 'l' || ch == 's' || ch == 'z')
//					k++;
//			} else if (m() == 1 && cvc(k))
//				setto("e");
//		}
//	}
//
//	/* step2() turns terminal y to i when there is another vowel in the stem. */
//
//	private final void step2() {
//		if (ends("y") && vowelinstem()) {
//			b[k] = 'i';
//			dirty = true;
//		}
//	}
//
//	/*
//	 * step3() maps double suffices to single ones. so -ization ( = -ize plus
//	 * -ation) maps to -ize etc. note that the string before the suffix must
//	 * give m() > 0.
//	 */
//
//	private final void step3() {
//		if (k == k0)
//			return; /* For Bug 1 */
//		switch (b[k - 1]) {
//		case 'a':
//			if (ends("ational")) {
//				r("ate");
//				break;
//			}
//			if (ends("tional")) {
//				r("tion");
//				break;
//			}
//			break;
//		case 'c':
//			if (ends("enci")) {
//				r("ence");
//				break;
//			}
//			if (ends("anci")) {
//				r("ance");
//				break;
//			}
//			break;
//		case 'e':
//			if (ends("izer")) {
//				r("ize");
//				break;
//			}
//			break;
//		case 'l':
//			if (ends("bli")) {
//				r("ble");
//				break;
//			}
//			if (ends("alli")) {
//				r("al");
//				break;
//			}
//			if (ends("entli")) {
//				r("ent");
//				break;
//			}
//			if (ends("eli")) {
//				r("e");
//				break;
//			}
//			if (ends("ousli")) {
//				r("ous");
//				break;
//			}
//			break;
//		case 'o':
//			if (ends("ization")) {
//				r("ize");
//				break;
//			}
//			if (ends("ation")) {
//				r("ate");
//				break;
//			}
//			if (ends("ator")) {
//				r("ate");
//				break;
//			}
//			break;
//		case 's':
//			if (ends("alism")) {
//				r("al");
//				break;
//			}
//			if (ends("iveness")) {
//				r("ive");
//				break;
//			}
//			if (ends("fulness")) {
//				r("ful");
//				break;
//			}
//			if (ends("ousness")) {
//				r("ous");
//				break;
//			}
//			break;
//		case 't':
//			if (ends("aliti")) {
//				r("al");
//				break;
//			}
//			if (ends("iviti")) {
//				r("ive");
//				break;
//			}
//			if (ends("biliti")) {
//				r("ble");
//				break;
//			}
//			break;
//		case 'g':
//			if (ends("logi")) {
//				r("log");
//				break;
//			}
//		}
//	}
//
//	/* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */
//
//	private final void step4() {
//		switch (b[k]) {
//		case 'e':
//			if (ends("icate")) {
//				r("ic");
//				break;
//			}
//			if (ends("ative")) {
//				r("");
//				break;
//			}
//			if (ends("alize")) {
//				r("al");
//				break;
//			}
//			break;
//		case 'i':
//			if (ends("iciti")) {
//				r("ic");
//				break;
//			}
//			break;
//		case 'l':
//			if (ends("ical")) {
//				r("ic");
//				break;
//			}
//			if (ends("ful")) {
//				r("");
//				break;
//			}
//			break;
//		case 's':
//			if (ends("ness")) {
//				r("");
//				break;
//			}
//			break;
//		}
//	}
//
//	/* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */
//
//	private final void step5() {
//		if (k == k0)
//			return; /* for Bug 1 */
//		switch (b[k - 1]) {
//		case 'a':
//			if (ends("al"))
//				break;
//			return;
//		case 'c':
//			if (ends("ance"))
//				break;
//			if (ends("ence"))
//				break;
//			return;
//		case 'e':
//			if (ends("er"))
//				break;
//			return;
//		case 'i':
//			if (ends("ic"))
//				break;
//			return;
//		case 'l':
//			if (ends("able"))
//				break;
//			if (ends("ible"))
//				break;
//			return;
//		case 'n':
//			if (ends("ant"))
//				break;
//			if (ends("ement"))
//				break;
//			if (ends("ment"))
//				break;
//			/* element etc. not stripped before the m */
//			if (ends("ent"))
//				break;
//			return;
//		case 'o':
//			if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't'))
//				break;
//			/* j >= 0 fixes Bug 2 */
//			if (ends("ou"))
//				break;
//			return;
//			/* takes care of -ous */
//		case 's':
//			if (ends("ism"))
//				break;
//			return;
//		case 't':
//			if (ends("ate"))
//				break;
//			if (ends("iti"))
//				break;
//			return;
//		case 'u':
//			if (ends("ous"))
//				break;
//			return;
//		case 'v':
//			if (ends("ive"))
//				break;
//			return;
//		case 'z':
//			if (ends("ize"))
//				break;
//			return;
//		default:
//			return;
//		}
//		if (m() > 1)
//			k = j;
//	}
//
//	/* step6() removes a final -e if m() > 1. */
//
//	private final void step6() {
//		j = k;
//		if (b[k] == 'e') {
//			int a = m();
//			if (a > 1 || a == 1 && !cvc(k - 1))
//				k--;
//		}
//		if (b[k] == 'l' && doublec(k) && m() > 1)
//			k--;
//	}
//
//	/**
//	 * Stem a word provided as a String. Returns the result as a String.
//	 */
//	public String stem(String s) {
//		if (stem(s.toCharArray(), s.length()))
//			return toString();
//		else
//			return s;
//	}
//
//	/**
//	 * Stem a word contained in a char[]. Returns true if the stemming process
//	 * resulted in a word different from the input. You can retrieve the result
//	 * with getResultLength()/getResultBuffer() or toString().
//	 */
//	public boolean stem(char[] word) {
//		return stem(word, word.length);
//	}
//
//	/**
//	 * Stem a word contained in a portion of a char[] array. Returns true if the
//	 * stemming process resulted in a word different from the input. You can
//	 * retrieve the result with getResultLength()/getResultBuffer() or
//	 * toString().
//	 */
//	public boolean stem(char[] wordBuffer, int offset, int wordLen) {
//		reset();
//		if (b.length < wordLen) {
//			b = new char[ArrayUtil.oversize(wordLen, NUM_BYTES_CHAR)];
//		}
//		System.arraycopy(wordBuffer, offset, b, 0, wordLen);
//		i = wordLen;
//		return stem(0);
//	}
//
//	/**
//	 * Stem a word contained in a leading portion of a char[] array. Returns
//	 * true if the stemming process resulted in a word different from the input.
//	 * You can retrieve the result with getResultLength()/getResultBuffer() or
//	 * toString().
//	 */
//	public boolean stem(char[] word, int wordLen) {
//		return stem(word, 0, wordLen);
//	}
//
//	/**
//	 * Stem the word placed into the Stemmer buffer through calls to add().
//	 * Returns true if the stemming process resulted in a word different from
//	 * the input. You can retrieve the result with
//	 * getResultLength()/getResultBuffer() or toString().
//	 */
//	public boolean stem() {
//		return stem(0);
//	}
//
//	public boolean stem(int i0) {
//		k = i - 1;
//		k0 = i0;
//		if (k > k0 + 1) {
//			step1();
//			step2();
//			step3();
//			step4();
//			step5();
//			step6();
//		}
//		// Also, a word is considered dirty if we lopped off letters
//		// Thanks to Ifigenia Vairelles for pointing this out.
//		if (i != k + 1)
//			dirty = true;
//		i = k + 1;
//		return dirty;
//	}
//
//	/**
//	 * Test program for demonstrating the Stemmer. It reads a file and stems
//	 * each word, writing the result to standard out. Usage: Stemmer file-name
//	 */
//	public static void main(String[] args) {
//		PorterStemmer s = new PorterStemmer();
//
//		for (int i = 0; i < args.length; i++) {
//			try {
//				InputStream in = new FileInputStream(args[i]);
//				byte[] buffer = new byte[1024];
//				int bufferLen, offset, ch;
//
//				bufferLen = in.read(buffer);
//				offset = 0;
//				s.reset();
//
//				while (true) {
//					if (offset < bufferLen)
//						ch = buffer[offset++];
//					else {
//						bufferLen = in.read(buffer);
//						offset = 0;
//						if (bufferLen < 0)
//							ch = -1;
//						else
//							ch = buffer[offset++];
//					}
//
//					if (Character.isLetter((char) ch)) {
//						s.add(Character.toLowerCase((char) ch));
//					} else {
//						s.stem();
//						System.out.print(s.toString());
//						s.reset();
//						if (ch < 0)
//							break;
//						else {
//							System.out.print((char) ch);
//						}
//					}
//				}
//
//				in.close();
//			} catch (IOException e) {
//				System.out.println("error reading " + args[i]);
//			}
//		}
//	}
//
//}

import org.apache.lucene.util.ArrayUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_CHAR;

abstract class PorterBase {
	protected char[] b;
	protected int k, j, k0;

	protected boolean endsWith(String s) {
		int l = s.length();
		int o = k - l + 1;
		if (o < 0) return false;
		for (int i = 0; i < l; i++) {
			if (b[o + i] != s.charAt(i)) return false;
		}
		j = k - l;
		return true;
	}

	protected void replaceIfMeasureGreaterThanZero(String s) {
		if (measureConsonantSequences() > 0) setTo(s);
	}

	protected int measureConsonantSequences() {
		int n = 0;
		int i = k0;
		while (true) {
			if (i > j) return n;
			if (!isConsonant(i)) break;
			i++;
		}
		i++;
		while (true) {
			while (true) {
				if (i > j) return n;
				if (isConsonant(i)) break;
				i++;
			}
			i++;
			n++;
			while (true) {
				if (i > j) return n;
				if (!isConsonant(i)) break;
				i++;
			}
			i++;
		}
	}

	protected boolean isConsonant(int i) {
		switch (b[i]) {
			case 'a': case 'e': case 'i': case 'o': case 'u':
				return false;
			case 'y':
				return (i == k0) ? true : !isConsonant(i - 1);
			default:
				return true;
		}
	}

	protected void setTo(String s) {
		int l = s.length();
		int o = j + 1;
		for (int i = 0; i < l; i++) {
			b[o + i] = s.charAt(i);
		}
		k = j + l;
	}
}

abstract class PorterStep extends PorterBase {
	PorterStep(char[] b, int k, int j, int k0) {
		this.b = b;
		this.k = k;
		this.j = j;
		this.k0 = k0;
	}

	abstract void apply();
}

class Step3 extends PorterStep {
	Step3(char[] b, int k, int j, int k0) {
		super(b, k, j, k0);
	}

	@Override
	void apply() {
		switch (b[k - 1]) {
			case 'a':
				if (endsWith("ational")) replaceIfMeasureGreaterThanZero("ate");
				else if (endsWith("tional")) replaceIfMeasureGreaterThanZero("tion");
				break;
			case 'c':
				if (endsWith("enci")) replaceIfMeasureGreaterThanZero("ence");
				else if (endsWith("anci")) replaceIfMeasureGreaterThanZero("ance");
				break;
			case 'e':
				if (endsWith("izer")) replaceIfMeasureGreaterThanZero("ize");
				break;
			case 'l':
				if (endsWith("bli")) replaceIfMeasureGreaterThanZero("ble");
				else if (endsWith("alli")) replaceIfMeasureGreaterThanZero("al");
				else if (endsWith("entli")) replaceIfMeasureGreaterThanZero("ent");
				else if (endsWith("eli")) replaceIfMeasureGreaterThanZero("e");
				else if (endsWith("ousli")) replaceIfMeasureGreaterThanZero("ous");
				break;
			case 'o':
				if (endsWith("ization")) replaceIfMeasureGreaterThanZero("ize");
				else if (endsWith("ation")) replaceIfMeasureGreaterThanZero("ate");
				else if (endsWith("ator")) replaceIfMeasureGreaterThanZero("ate");
				break;
			case 's':
				if (endsWith("alism")) replaceIfMeasureGreaterThanZero("al");
				else if (endsWith("iveness")) replaceIfMeasureGreaterThanZero("ive");
				else if (endsWith("fulness")) replaceIfMeasureGreaterThanZero("ful");
				else if (endsWith("ousness")) replaceIfMeasureGreaterThanZero("ous");
				break;
			case 't':
				if (endsWith("aliti")) replaceIfMeasureGreaterThanZero("al");
				else if (endsWith("iviti")) replaceIfMeasureGreaterThanZero("ive");
				else if (endsWith("biliti")) replaceIfMeasureGreaterThanZero("ble");
				break;
			case 'g':
				if (endsWith("logi")) replaceIfMeasureGreaterThanZero("log");
				break;
		}
	}
}

class Step4 extends PorterStep {
	Step4(char[] b, int k, int j, int k0) {
		super(b, k, j, k0);
	}

	@Override
	void apply() {
		switch (b[k]) {
			case 'e':
				if (endsWith("icate")) replaceIfMeasureGreaterThanZero("ic");
				else if (endsWith("ative")) replaceIfMeasureGreaterThanZero("");
				else if (endsWith("alize")) replaceIfMeasureGreaterThanZero("al");
				break;
			case 'i':
				if (endsWith("iciti")) replaceIfMeasureGreaterThanZero("ic");
				break;
			case 'l':
				if (endsWith("ical")) replaceIfMeasureGreaterThanZero("ic");
				else if (endsWith("ful")) replaceIfMeasureGreaterThanZero("");
				break;
			case 's':
				if (endsWith("ness")) replaceIfMeasureGreaterThanZero("");
				break;
		}
	}
}

class Step5 extends PorterStep {
	Step5(char[] b, int k, int j, int k0) {
		super(b, k, j, k0);
	}

	@Override
	void apply() {
		if (k == k0) return;
		switch (b[k - 1]) {
			case 'a':
				if (endsWith("al")) break;
				return;
			case 'c':
				if (endsWith("ance") || endsWith("ence")) break;
				return;
			case 'e':
				if (endsWith("er")) break;
				return;
			case 'i':
				if (endsWith("ic")) break;
				return;
			case 'l':
				if (endsWith("able") || endsWith("ible")) break;
				return;
			case 'n':
				if (endsWith("ant") || endsWith("ement") || endsWith("ment") || endsWith("ent")) break;
				return;
			case 'o':
				if ((endsWith("ion") && (j >= 0 && (b[j] == 's' || b[j] == 't'))) || endsWith("ou")) break;
				return;
			case 's':
				if (endsWith("ism")) break;
				return;
			case 't':
				if (endsWith("ate") || endsWith("iti")) break;
				return;
			case 'u':
				if (endsWith("ous")) break;
				return;
			case 'v':
				if (endsWith("ive")) break;
				return;
			case 'z':
				if (endsWith("ize")) break;
				return;
			default:
				return;
		}
		if (measureConsonantSequences() > 1) k = j;
	}
}

public class PorterStemmer extends PorterBase {

	private int i;
	private boolean dirty = false;
	private static final int INITIAL_SIZE = 50;

	public PorterStemmer() {
		b = new char[INITIAL_SIZE];
		i = 0;
	}

	public void reset() {
		i = 0;
		dirty = false;
	}

	public void add(char ch) {
		if (b.length <= i) {
			b = ArrayUtil.grow(b, i + 1);
		}
		b[i++] = ch;
	}

	@Override
	public String toString() {
		return new String(b, 0, i);
	}

	public int getResultLength() {
		return i;
	}

	public char[] getResultBuffer() {
		return b;
	}

	private boolean containsVowel() {
		for (int i = k0; i <= j; i++) {
			if (!isConsonant(i)) return true;
		}
		return false;
	}

	private boolean cvc(int i) {
		if (i < k0 + 2 || !isConsonant(i) || isConsonant(i - 1) || !isConsonant(i - 2)) return false;
		int ch = b[i];
		return ch != 'w' && ch != 'x' && ch != 'y';
	}

	private void handleSuffixedWithS() {
		if (endsWith("sses")) k -= 2;
		else if (endsWith("ies")) setTo("i");
		else if (b[k - 1] != 's') k--;
	}

	private void handleEedSuffix() {
		if (endsWith("eed")) {
			if (measureConsonantSequences() > 0) k--;
		}
	}

	private void handleEdOrIngSuffix() {
		if ((endsWith("ed") || endsWith("ing")) && containsVowel()) {
			k = j;
			if (endsWith("at")) setTo("ate");
			else if (endsWith("bl")) setTo("ble");
			else if (endsWith("iz")) setTo("ize");
			else if (doubleConsonant(k)) {
				int ch = b[k--];
				if (ch == 'l' || ch == 's' || ch == 'z') k++;
			} else if (measureConsonantSequences() == 1 && cvc(k)) setTo("e");
		}
	}

	private boolean doubleConsonant(int j) {
		if (j < k0 + 1) return false;
		return b[j] == b[j - 1] && isConsonant(j);
	}

	private void step1() {
		if (b[k] == 's') {
			handleSuffixedWithS();
		}
		handleEedSuffix();
		handleEdOrIngSuffix();
	}

	private void step2() {
		if (endsWith("y") && containsVowel()) {
			b[k] = 'i';
			dirty = true;
		}
	}

	private void step3() {
		Step3 step3 = new Step3(b, k, j, k0);
		step3.apply();
	}

	private void step4() {
		Step4 step4 = new Step4(b, k, j, k0);
		step4.apply();
	}

	private void step5() {
		Step5 step5 = new Step5(b, k, j, k0);
		step5.apply();
	}

	private void step6() {
		j = k;
		if (b[k] == 'e') {
			int a = measureConsonantSequences();
			if (a > 1 || (a == 1 && !cvc(k - 1))) k--;
		}
		if (b[k] == 'l' && doubleConsonant(k) && measureConsonantSequences() > 1) k--;
	}

	public String stem(String s) {
		if (stem(s.toCharArray(), s.length())) return toString();
		else return s;
	}

	public boolean stem(char[] word) {
		return stem(word, word.length);
	}

	public boolean stem(char[] wordBuffer, int offset, int wordLen) {
		reset();
		if (b.length < wordLen) {
			b = new char[ArrayUtil.oversize(wordLen, NUM_BYTES_CHAR)];
		}
		System.arraycopy(wordBuffer, offset, b, 0, wordLen);
		i = wordLen;
		return stem(0);
	}

	public boolean stem(char[] word, int wordLen) {
		return stem(word, 0, wordLen);
	}

	public boolean stem() {
		return stem(0);
	}

	private boolean stem(int i0) {
		k = i - 1;
		k0 = i0;
		if (k > k0 + 1) {
			step1();
			step2();
			step3();
			step4();
			step5();
			step6();
		}
		if (i != k + 1) dirty = true;
		i = k + 1;
		return dirty;
	}

	public static void main(String[] args) {
		PorterStemmer s = new PorterStemmer();

		for (String arg : args) {
			try (InputStream in = new FileInputStream(arg)) {
				byte[] buffer = new byte[1024];
				int bufferLen, offset, ch;

				bufferLen = in.read(buffer);
				offset = 0;
				s.reset();

				while (true) {
					if (offset < bufferLen) {
						ch = buffer[offset++];
					} else {
						bufferLen = in.read(buffer);
						offset = 0;
						if (bufferLen < 0) ch = -1;
						else ch = buffer[offset++];
					}

					if (Character.isLetter((char) ch)) {
						s.add(Character.toLowerCase((char) ch));
					} else {
						s.stem();
						System.out.print(s.toString());
						s.reset();
						if (ch < 0) break;
						else System.out.print((char) ch);
					}
				}
			} catch (IOException e) {
				System.out.println("error reading " + arg);
			}
		}
	}
}