package xttran.classifier.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import xttran.classifier.util.LineParser;

public class TestLineParser {
	@Test
	public void testParse() {
		Object lineData  = "trade	asian exporters fear damage japan rift mounting trade friction and japan raised fears asia exporting nations that row inflict reaching economic damage businessmen and officials told reuter correspondents asian capitals move japan boost protectionist sentiment and lead curbs american imports products exporters that conflict hurt long run short term tokyo loss gain will impose mln dlrs tariffs imports japanese electronics goods april retaliation for japan alleged failure stick pact not sell semiconductors world markets below cost unofficial japanese estimates put impact tariffs billion dlrs and spokesmen for major electronics firms virtually halt exports products hit taxes wouldn business spokesman for leading japanese electronics firm matsushita electric industrial tariffs remain place for length time months will mean complete erosion exports goods subject tariffs tom murtha stock analyst tokyo office broker james capel and taiwan businessmen and officials worried aware seriousness threat japan serves warning senior taiwanese trade official asked not named taiwan had trade trade surplus billion dlrs last year pct surplus helped swell taiwan foreign exchange reserves billion dlrs world largest quickly open markets remove trade barriers and cut import tariffs imports products want defuse problems retaliation paul sheen chairman textile exporters taiwan safe group senior official south korea trade promotion association trade dispute and japan lead pressure south korea chief exports similar japan last year south korea had trade surplus billion dlrs billion dlrs malaysia trade officers and businessmen tough curbs japan hard hit producers semiconductors countries expand sales hong kong newspapers alleged japan selling below cost semiconductors electronics manufacturers share that view businessmen such short term commercial advantage outweighed pressure block imports that short term view lawrence mills director general federation hong kong industry purpose prevent imports day will extended sources for hong kong disadvantage action restraining trade last year hong kong biggest export market accounting for pct domestically produced exports australian government awaiting outcome trade talks and japan interest and concern industry minister john button canberra last friday this kind deterioration trade relations two countries major trading partners ours matter button australia concerns centred coal and beef australia two largest exports japan and significant exports that country meanwhile japanese diplomatic manoeuvres solve trade stand continue japan ruling liberal democratic party yesterday outlined package economic measures boost japanese economy measures proposed include large supplementary budget and record public works spending half financial year call for stepped spending emergency measure stimulate economy despite prime minister yasuhiro nakasone avowed fiscal reform program deputy trade representative michael smith and makoto kuroda japan deputy minister international trade and industry miti due meet washington this week effort end dispute reuter";
		List<String> words = new ArrayList<String>(); 
		
		String category = LineParser.parse(lineData, words);
		System.out.println(category);
		System.out.println(words);
	}
}
