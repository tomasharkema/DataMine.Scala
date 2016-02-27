package main

import org.specs2.mutable.Specification

import Helpers._

/**
  * Created by tomas on 27-02-16.
  */
class HelpersStreamTest extends Specification {

  "a Stream of items" should {
    "be grouped by first character and single items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(_.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap"))
      grouped should havePair("N" -> List("Noot"))
      grouped should havePair("M" -> List("Mies"))
      grouped should havePair("W" -> List("Wim"))
      grouped should havePair("Z" -> List("Zus"))
      grouped should havePair("J" -> List("Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }

    "be grouped by first character and double items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(_.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap", "Aap"))
      grouped should havePair("N" -> List("Noot", "Noot"))
      grouped should havePair("M" -> List("Mies", "Mies"))
      grouped should havePair("W" -> List("Wim", "Wim"))
      grouped should havePair("Z" -> List("Zus", "Zus"))
      grouped should havePair("J" -> List("Jet", "Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }

    "be grouped by first character and triple items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(_.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap", "Aap", "Aap"))
      grouped should havePair("N" -> List("Noot", "Noot", "Noot"))
      grouped should havePair("M" -> List("Mies", "Mies", "Mies"))
      grouped should havePair("W" -> List("Wim", "Wim", "Wim"))
      grouped should havePair("Z" -> List("Zus", "Zus", "Zus"))
      grouped should havePair("J" -> List("Jet", "Jet", "Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }

    "be nice to Iterators" in {

      val stream = Stream(
        ("A", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("A", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("A", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("A", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("B", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("B", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("B", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("B", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("C", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("C", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("C", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("C", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("D", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("D", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("D", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")),
        ("D", Iterator("Aap", "Noot", "Mies", "Wim", "Zus", "Jet"))
      )

      val grouped = stream
        .groupByKeyAndValue(_._1)(_._2.toStream)
        .map(el => (el._1, el._2.toList))

      grouped should have size 4

      grouped should haveKeys("A", "B", "C", "D")

      Seq("A", "B", "C", "D").map { el =>
        grouped(el) must contain(be("Aap")).exactly(4)
        grouped(el) must contain(be("Noot")).exactly(4)
        grouped(el) must contain(be("Mies")).exactly(4)
        grouped(el) must contain(be("Wim")).exactly(4)
        grouped(el) must contain(be("Zus")).exactly(4)
        grouped(el) must contain(be("Jet")).exactly(4)
      }
    }
  }

  "Grouped from a SeparatorIterator" should {
    "so yeah" in {
      val stream = Stream(
        Iterator("Belastingen", "Gisteren ontving ik, tot mijn verbazing, opnieuw een aanmaning betreffende de mij opgelegde aanslag 31449952 waarbij gedreigd wordt met allerlei maatregelen als ik de aanslag niet binnen enkele dagen betaal. <br />Enkele weken geleden (direct na ontvangst van de eerste aanslag) heb ik echter bezwaar<wbr />/beroep aangetekend tegen deze aanslag. Hier heb ik nog totaal niets over gehoord, zelfs geen ontvangstbevestiging. U zult begrijpen dat het mij vreemd overkomt dat ik nu wel gewoon deze aanmaning ontvang. Het lijkt erop dat de gemeente Amsterdam mijn bezwaar<wbr />/beroep gewoon naast zich neerlegt en lekker doorgaat met de invordering van de aanslag. <br /><br />Ik zou dan ook, voordat ik eventueel tot betaling overga, graag eerst vernemen hoe het precies staat met mijn bezwaar<wbr />/beroep. Hoe en wanneer wordt dit behandeld, wat is de uitslag ervan, etc. <br /><br /><br />Mede omdat u ook dreigt met het inschakelen van deurwaarders en allerlei bijbehorende incassomaatregelen zou ik dan ook graag van u het volg", "?", "Afhandeling lopend bezwaar<wbr />/beroep"),
        Iterator("Belastingen", "BILJETNUMMER 36043336", "", "I HAVE RECENTLY MOVED INTO MY HOUSE ON WILLEM FREDERIK HERMANSSTRAAT - I WOULD LIKE TO KNOW THE TYPES OF TAX THAT I HAVE TO PAY AND HOW AM I CHARGED? I WAS ISSUED A BILJET OF EUR41.08 PER MONTH - THIS IS WAY MORE THAN WHAT I EXPECT TO PAY PER MONTH. PLEASE COULD YOU EXPLAIN?"),
        Iterator("Openbare ruimte", "Stank en geluidsoverlast door bakkerij die zich onder onze koopwoning , van de alliantie heeft gevestigd ! ", "", "Inspectie en kijken of deze bakkerij zich hier uberhaupt wel mag vestigen !! <br />Kijken naar afzuiginstalatie en geluiden . "),
        Iterator("Belastingen", "Hallo, ik heb een brief ontvangen over een betaling gaat over een bepaald aanslagbedrag met twee acceptgiro's<br /><br />het gaat om een aanslagbiljet reclamebelasting definitieve aanslag<br /><br />biljetnummer 36067702<br />belastingjaar 2014<br />dagtekening 30-09-2014<br />betalingskenmerk 3000 0000 3606 7702<br />totaalbedrag €201,50<br /><br />de reden van de klacht is namelijk dat Spaans Bedrijf Restaurant La Paella B.V. sinds 1 maart 2014 niet meer actief is als B.V. het bedrijf is verkocht en het is nu een V.O.F.", "", "Dat het aangepast word.<br />want spaans restaurant bedrijf la paella b.v. is op 1 maart verkocht, het is nu een V.O.F. en de eigenaar is een ander persoon nu. <br /><br />La Paella B.V. is nog ingeschreven als naam op de kamer van koophandel maar het is niet meer actief als onderneming.<br /><br />dus dat jullie het onder de naam v.o.f. zetten en op het correcte adres versturen is wat jullie kunnen doen.")
      )

      val grouped = stream
        .map { el =>
          val e = el.toStream
          new {
            val key = e.head
            val value = e.slice(1, 4)
          }
        }
        .groupByKeyAndValue(_.key)(_.value.toStream)
        .map(el => (el._1, el._2.toList))

      grouped("Belastingen") should contain("BILJETNUMMER 36043336")
      grouped("Belastingen") should contain("Afhandeling lopend bezwaar<wbr />/beroep")

    }
  }

}
