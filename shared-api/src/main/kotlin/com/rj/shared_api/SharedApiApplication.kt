package com.rj.shared_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SharedApiApplication

fun main(args: Array<String>) {
	runApplication<SharedApiApplication>(*args)
}
