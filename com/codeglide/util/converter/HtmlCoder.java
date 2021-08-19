/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */

package com.codeglide.util.converter;

/**
 * @author admin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HtmlCoder {
	
	public static String encodeHtmlEntity(int c) {
		if( c == '<' )
			return "&lt;";
		else if( c == '>' )
			return "&gt;";
		else if( c == '&' )
			return "&amp;";
		else if( c == '"' )
			return "&quot;";
		else
			return String.valueOf((char)c);
	}
	
	public static int decodeHtmlEntity(String val) {
		if( val != null && val.length() > 0 ) {
			if( val.charAt(0) == '#' ) {
				int radix = 10;
				val = val.substring(1);
				if( Character.toLowerCase(val.charAt(0)) == 'x' ) {
					val = val.substring(1);
					radix = 16;
				}
				try {
					return (char) Integer.parseInt(val, radix);
				} catch (Exception e) {}
			} else {
				if( val.equals("lt") ) return('<');
				else if( val.equals("gt") ) return('>');
				else if( val.equals("amp") ) return('&');
				else if( val.equals("quot") ) return('\"');
				else if( val.equals("nbsp") ) return(' ');
				else if( val.equals("iexcl") ) return(161);
				else if( val.equals("cent") ) return(162);
				else if( val.equals("pound") ) return(163);
				else if( val.equals("curren") ) return(164);
				else if( val.equals("yen") ) return(165);
				else if( val.equals("brvbar") ) return(166);
				else if( val.equals("sect") ) return(167);
				else if( val.equals("uml") ) return(168);
				else if( val.equals("copy") ) return(169);
				else if( val.equals("ordf") ) return(170);
				else if( val.equals("laquo") ) return(171);
				else if( val.equals("not") ) return(172);
				else if( val.equals("shy") ) return(173);
				else if( val.equals("reg") ) return(174);
				else if( val.equals("macr") ) return(175);
				else if( val.equals("deg") ) return(176);
				else if( val.equals("plusmn") ) return(177);
				else if( val.equals("sup2") ) return(178);
				else if( val.equals("sup3") ) return(179);
				else if( val.equals("acute") ) return(180);
				else if( val.equals("micro") ) return(181);
				else if( val.equals("para") ) return(182);
				else if( val.equals("middot") ) return(183);
				else if( val.equals("cedil") ) return(184);
				else if( val.equals("sup1") ) return(185);
				else if( val.equals("ordm") ) return(186);
				else if( val.equals("raquo") ) return(187);
				else if( val.equals("frac14") ) return(188);
				else if( val.equals("frac12") ) return(189);
				else if( val.equals("frac34") ) return(190);
				else if( val.equals("iquest") ) return(191);
				else if( val.equals("Agrave") ) return(192);
				else if( val.equals("Aacute") ) return(193);
				else if( val.equals("Acirc") ) return(194);
				else if( val.equals("Atilde") ) return(195);
				else if( val.equals("Auml") ) return(196);
				else if( val.equals("Aring") ) return(197);
				else if( val.equals("AElig") ) return(198);
				else if( val.equals("Ccedil") ) return(199);
				else if( val.equals("Egrave") ) return(200);
				else if( val.equals("Eacute") ) return(201);
				else if( val.equals("Ecirc") ) return(202);
				else if( val.equals("Euml") ) return(203);
				else if( val.equals("Igrave") ) return(204);
				else if( val.equals("Iacute") ) return(205);
				else if( val.equals("Icirc") ) return(206);
				else if( val.equals("Iuml") ) return(207);
				else if( val.equals("ETH") ) return(208);
				else if( val.equals("Ntilde") ) return(209);
				else if( val.equals("Ograve") ) return(210);
				else if( val.equals("Oacute") ) return(211);
				else if( val.equals("Ocirc") ) return(212);
				else if( val.equals("Otilde") ) return(213);
				else if( val.equals("Ouml") ) return(214);
				else if( val.equals("times") ) return(215);
				else if( val.equals("Oslash") ) return(216);
				else if( val.equals("Ugrave") ) return(217);
				else if( val.equals("Uacute") ) return(218);
				else if( val.equals("Ucirc") ) return(219);
				else if( val.equals("Uuml") ) return(220);
				else if( val.equals("Yacute") ) return(221);
				else if( val.equals("THORN") ) return(222);
				else if( val.equals("szlig") ) return(223);
				else if( val.equals("agrave") ) return(224);
				else if( val.equals("aacute") ) return(225);
				else if( val.equals("acirc") ) return(226);
				else if( val.equals("atilde") ) return(227);
				else if( val.equals("auml") ) return(228);
				else if( val.equals("aring") ) return(229);
				else if( val.equals("aelig") ) return(230);
				else if( val.equals("ccedil") ) return(231);
				else if( val.equals("egrave") ) return(232);
				else if( val.equals("eacute") ) return(233);
				else if( val.equals("ecirc") ) return(234);
				else if( val.equals("euml") ) return(235);
				else if( val.equals("igrave") ) return(236);
				else if( val.equals("iacute") ) return(237);
				else if( val.equals("icirc") ) return(238);
				else if( val.equals("iuml") ) return(239);
				else if( val.equals("eth") ) return(240);
				else if( val.equals("ntilde") ) return(241);
				else if( val.equals("ograve") ) return(242);
				else if( val.equals("oacute") ) return(243);
				else if( val.equals("ocirc") ) return(244);
				else if( val.equals("otilde") ) return(245);
				else if( val.equals("ouml") ) return(246);
				else if( val.equals("divide") ) return(247);
				else if( val.equals("oslash") ) return(248);
				else if( val.equals("ugrave") ) return(249);
				else if( val.equals("uacute") ) return(250);
				else if( val.equals("ucirc") ) return(251);
				else if( val.equals("uuml") ) return(252);
				else if( val.equals("yacute") ) return(253);
				else if( val.equals("thorn") ) return(254);
				else if( val.equals("yuml") ) return(255);
				else if( val.equals("fnof") ) return(402);
				else if( val.equals("Alpha") ) return(913);
				else if( val.equals("Beta") ) return(914);
				else if( val.equals("Gamma") ) return(915);
				else if( val.equals("Delta") ) return(916);
				else if( val.equals("Epsilon") ) return(917);
				else if( val.equals("Zeta") ) return(918);
				else if( val.equals("Eta") ) return(919);
				else if( val.equals("Theta") ) return(920);
				else if( val.equals("Iota") ) return(921);
				else if( val.equals("Kappa") ) return(922);
				else if( val.equals("Lambda") ) return(923);
				else if( val.equals("Mu") ) return(924);
				else if( val.equals("Nu") ) return(925);
				else if( val.equals("Xi") ) return(926);
				else if( val.equals("Omicron") ) return(927);
				else if( val.equals("Pi") ) return(928);
				else if( val.equals("Rho") ) return(929);
				else if( val.equals("Sigma") ) return(931);
				else if( val.equals("Tau") ) return(932);
				else if( val.equals("Upsilon") ) return(933);
				else if( val.equals("Phi") ) return(934);
				else if( val.equals("Chi") ) return(935);
				else if( val.equals("Psi") ) return(936);
				else if( val.equals("Omega") ) return(937);
				else if( val.equals("alpha") ) return(945);
				else if( val.equals("beta") ) return(946);
				else if( val.equals("gamma") ) return(947);
				else if( val.equals("delta") ) return(948);
				else if( val.equals("epsilon") ) return(949);
				else if( val.equals("zeta") ) return(950);
				else if( val.equals("eta") ) return(951);
				else if( val.equals("theta") ) return(952);
				else if( val.equals("iota") ) return(953);
				else if( val.equals("kappa") ) return(954);
				else if( val.equals("lambda") ) return(955);
				else if( val.equals("mu") ) return(956);
				else if( val.equals("nu") ) return(957);
				else if( val.equals("xi") ) return(958);
				else if( val.equals("omicron") ) return(959);
				else if( val.equals("pi") ) return(960);
				else if( val.equals("rho") ) return(961);
				else if( val.equals("sigmaf") ) return(962);
				else if( val.equals("sigma") ) return(963);
				else if( val.equals("tau") ) return(964);
				else if( val.equals("upsilon") ) return(965);
				else if( val.equals("phi") ) return(966);
				else if( val.equals("chi") ) return(967);
				else if( val.equals("psi") ) return(968);
				else if( val.equals("omega") ) return(969);
				else if( val.equals("thetasym") ) return(977);
				else if( val.equals("upsih") ) return(978);
				else if( val.equals("piv") ) return(982);
				else if( val.equals("bull") ) return(8226);
				else if( val.equals("hellip") ) return(8230);
				else if( val.equals("prime") ) return(8242);
				else if( val.equals("Prime") ) return(8243);
				else if( val.equals("oline") ) return(8254);
				else if( val.equals("frasl") ) return(8260);
				else if( val.equals("weierp") ) return(8472);
				else if( val.equals("image") ) return(8465);
				else if( val.equals("real") ) return(8476);
				else if( val.equals("trade") ) return(8482);
				else if( val.equals("alefsym") ) return(8501);
				else if( val.equals("larr") ) return(8592);
				else if( val.equals("uarr") ) return(8593);
				else if( val.equals("rarr") ) return(8594);
				else if( val.equals("darr") ) return(8595);
				else if( val.equals("harr") ) return(8596);
				else if( val.equals("crarr") ) return(8629);
				else if( val.equals("lArr") ) return(8656);
				else if( val.equals("uArr") ) return(8657);
				else if( val.equals("rArr") ) return(8658);
				else if( val.equals("dArr") ) return(8659);
				else if( val.equals("hArr") ) return(8660);
				else if( val.equals("forall") ) return(8704);
				else if( val.equals("part") ) return(8706);
				else if( val.equals("exist") ) return(8707);
				else if( val.equals("empty") ) return(8709);
				else if( val.equals("nabla") ) return(8711);
				else if( val.equals("isin") ) return(8712);
				else if( val.equals("notin") ) return(8713);
				else if( val.equals("ni") ) return(8715);
				else if( val.equals("prod") ) return(8719);
				else if( val.equals("sum") ) return(8721);
				else if( val.equals("minus") ) return(8722);
				else if( val.equals("lowast") ) return(8727);
				else if( val.equals("radic") ) return(8730);
				else if( val.equals("prop") ) return(8733);
				else if( val.equals("infin") ) return(8734);
				else if( val.equals("ang") ) return(8736);
				else if( val.equals("and") ) return(8743);
				else if( val.equals("or") ) return(8744);
				else if( val.equals("cap") ) return(8745);
				else if( val.equals("cup") ) return(8746);
				else if( val.equals("int") ) return(8747);
				else if( val.equals("there4") ) return(8756);
				else if( val.equals("sim") ) return(8764);
				else if( val.equals("cong") ) return(8773);
				else if( val.equals("asymp") ) return(8776);
				else if( val.equals("ne") ) return(8800);
				else if( val.equals("equiv") ) return(8801);
				else if( val.equals("le") ) return(8804);
				else if( val.equals("ge") ) return(8805);
				else if( val.equals("sub") ) return(8834);
				else if( val.equals("sup") ) return(8835);
				else if( val.equals("nsub") ) return(8836);
				else if( val.equals("sube") ) return(8838);
				else if( val.equals("supe") ) return(8839);
				else if( val.equals("oplus") ) return(8853);
				else if( val.equals("otimes") ) return(8855);
				else if( val.equals("perp") ) return(8869);
				else if( val.equals("sdot") ) return(8901);
				else if( val.equals("lceil") ) return(8968);
				else if( val.equals("rceil") ) return(8969);
				else if( val.equals("lfloor") ) return(8970);
				else if( val.equals("rfloor") ) return(8971);
				else if( val.equals("lang") ) return(9001);
				else if( val.equals("rang") ) return(9002);
				else if( val.equals("loz") ) return(9674);
				else if( val.equals("spades") ) return(9824);
				else if( val.equals("clubs") ) return(9827);
				else if( val.equals("hearts") ) return(9829);
				else if( val.equals("diams") ) return(9830);
				else if( val.equals("quot") ) return(34);
				else if( val.equals("amp") ) return(38);
				else if( val.equals("lt") ) return(60);
				else if( val.equals("gt") ) return(62);
				else if( val.equals("OElig") ) return(338);
				else if( val.equals("oelig") ) return(339);
				else if( val.equals("Scaron") ) return(352);
				else if( val.equals("scaron") ) return(353);
				else if( val.equals("Yuml") ) return(376);
				else if( val.equals("circ") ) return(710);
				else if( val.equals("tilde") ) return(732);
				else if( val.equals("ensp") ) return(8194);
				else if( val.equals("emsp") ) return(8195);
				else if( val.equals("thinsp") ) return(8201);
				else if( val.equals("zwnj") ) return(8204);
				else if( val.equals("zwj") ) return(8205);
				else if( val.equals("lrm") ) return(8206);
				else if( val.equals("rlm") ) return(8207);
				else if( val.equals("ndash") ) return(8211);
				else if( val.equals("mdash") ) return(8212);
				else if( val.equals("lsquo") ) return(8216);
				else if( val.equals("rsquo") ) return(8217);
				else if( val.equals("sbquo") ) return(8218);
				else if( val.equals("ldquo") ) return(8220);
				else if( val.equals("rdquo") ) return(8221);
				else if( val.equals("bdquo") ) return(8222);
				else if( val.equals("dagger") ) return(8224);
				else if( val.equals("Dagger") ) return(8225);
				else if( val.equals("permil") ) return(8240);
				else if( val.equals("lsaquo") ) return(8249);
				else if( val.equals("rsaquo") ) return(8250);
				else if( val.equals("euro") ) return(8364);
			}
		}
		return 0;
	}


}
