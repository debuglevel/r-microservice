package de.debuglevel.latex.rest

import de.debuglevel.latex.rest.latex.MarkdownController
import de.debuglevel.microservices.utils.apiversion.apiVersion
import de.debuglevel.microservices.utils.logging.buildRequestLog
import de.debuglevel.microservices.utils.logging.buildResponseLog
import de.debuglevel.microservices.utils.spark.configuredPort
import de.debuglevel.microservices.utils.status.status
import mu.KotlinLogging
import spark.Spark.path
import spark.kotlin.after
import spark.kotlin.before
import spark.kotlin.get
import spark.kotlin.post


/**
 * REST endpoint
 */
class RestEndpoint {
    private val logger = KotlinLogging.logger {}

    /**
     * Starts the REST endpoint to enter a listening state
     *
     * @param args parameters to be passed from main() command line
     */
    fun start(args: Array<String>) {
        logger.info("Starting...")
        configuredPort()
        status(this::class.java)

        apiVersion("1", true)
        {
            path("/documents") {
                post("/", function = MarkdownController.postOne())

                path("/:id") {
                    get("", "text/html", MarkdownController.getOneHtml())
                    get("/", "text/html", MarkdownController.getOneHtml())
                    get("", "text/plain", MarkdownController.getOnePlaintext())
                    get("/", "text/plain", MarkdownController.getOnePlaintext())
                    get("", "application/json", MarkdownController.getOneJson())
                    get("/", "application/json", MarkdownController.getOneJson())
                }
            }
        }

        // add loggers
        before { logger.debug(buildRequestLog(request)) }
        after { logger.debug(buildResponseLog(request, response)) }
    }
}
