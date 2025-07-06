package org.kevin.trello_v2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TrelloV2Application

fun main(args: Array<String>) {
	runApplication<TrelloV2Application>(*args)
}
