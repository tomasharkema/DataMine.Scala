package main

import org.specs2.mutable.Specification
import helpers.StopWords._
/**
  * Created by tomas on 01-05-16.
  */
class FilterStopWordsTest extends Specification {

  "a stopworkdfilter" should {
    "filter only whole words" in {
      val s = "Gisteren ontving ik, tot mijn verbazing, opnieuw een aanmaning betreffende de mij opgelegde aanslag 31449952 waarbij gedreigd wordt met allerlei maatregelen als ik de aanslag niet binnen enkele dagen betaal. <br />Enkele weken geleden (direct na ontvangst van de eerste aanslag) heb ik echter bezwaar<wbr />/beroep aangetekend tegen deze aanslag. Hier heb ik nog totaal niets over gehoord, zelfs geen ontvangstbevestiging. U zult begrijpen dat het mij vreemd overkomt dat ik nu wel gewoon deze aanmaning ontvang. Het lijkt erop dat de gemeente Amsterdam mijn bezwaar<wbr />/beroep gewoon naast zich neerlegt en lekker doorgaat met de invordering van de aanslag. <br /><br />Ik zou dan ook, voordat ik eventueel tot betaling overga, graag eerst vernemen hoe het precies staat met mijn bezwaar<wbr />/beroep. Hoe en wanneer wordt dit behandeld, wat is de uitslag ervan, etc. <br /><br /><br />Mede omdat u ook dreigt met het inschakelen van deurwaarders en allerlei bijbehorende incassomaatregelen zou ik dan ook graag van u het volg;? ;Afhandeling lopend bezwaar<wbr />/beroep;\nBelastingen;BILJETNUMMER 36043336;;I HAVE RECENTLY MOVED INTO MY HOUSE ON WILLEM FREDERIK HERMANSSTRAAT - I WOULD LIKE TO KNOW THE TYPES OF TAX THAT I HAVE TO PAY AND HOW AM I CHARGED? I WAS ISSUED A BILJET OF EUR41.08 PER MONTH - THIS IS WAY MORE THAN WHAT I EXPECT TO PAY PER MONTH. PLEASE COULD YOU EXPLAIN?;\nOpenbare ruimte;Stank en geluidsoverlast door bakkerij die zich onder onze koopwoning , van de alliantie heeft gevestigd ! ;Her geven van vergunning voor bakkerij onder woning ????;Inspectie en kijken of deze bakkerij zich hier uberhaupt wel mag vestigen !! <br />Kijken naar afzuiginstalatie en geluiden.;"
      val filtered = s.filterStopWords
      println(filtered)

      filtered must be equalTo "Gisteren ontving ik, mijn verbazing, opnieuw aanmaning betreffende mij opgelegde aanslag 31449952 waarbij gedreigd allerlei maatregelen aanslag binnen dagen betaal. <br />Enkele weken geleden (direct ontvangst eerste aanslag) heb echter bezwaar<wbr />/beroep aangetekend aanslag. Hier heb nog totaal niets gehoord, zelfs geen ontvangstbevestiging. U zult begrijpen mij vreemd overkomt wel gewoon aanmaning ontvang. Het lijkt erop gemeente Amsterdam mijn bezwaar<wbr />/beroep gewoon naast neerlegt lekker doorgaat invordering aanslag. <br /><br />Ik zou dan ook, voordat eventueel betaling overga, graag eerst vernemen precies staat mijn bezwaar<wbr />/beroep. Hoe wanneer behandeld, uitslag ervan, etc. <br /><br /><br />Mede omdat dreigt inschakelen deurwaarders allerlei bijbehorende incassomaatregelen zou dan graag volg;? ;Afhandeling lopend bezwaar<wbr />/beroep;\nBelastingen;BILJETNUMMER 36043336;;I HAVE RECENTLY MOVED INTO MY HOUSE ON WILLEM FREDERIK HERMANSSTRAAT - I WOULD LIKE TO KNOW THE TYPES OF TAX THAT I HAVE TO PAY AND HOW AM I CHARGED? I WAS ISSUED A BILJET OF EUR41.08 PER MONTH - THIS IS WAY MORE THAN WHAT I EXPECT TO PAY PER MONTH. PLEASE COULD YOU EXPLAIN?;"
    }
  }

}
