package org.hfernandes.wa

import spark.ModelAndView
import spark.template.velocity.VelocityTemplateEngine
import spark.utils.IOUtils
import javax.servlet.MultipartConfigElement
import javax.servlet.http.Part
import static spark.Spark.*


class Main {

    static void main(String[] args){
        port(9090)
        staticFileLocation("/static")
        post("/upload", { req, res ->
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty('java.io.tmpdir'))
            req.raw().setAttribute('org.eclipse.jetty.multipartConfig', multipartConfigElement)

            Part part = req.raw().getPart('file')
            File csv = File.createTempFile('upload', '.csv')

            try {
                csv.withOutputStream { OutputStream outStream ->
                    IOUtils.copy(part.getInputStream(), outStream)
                }

                new ModelAndView([data: CSVFilter.filter(csv)], "output.vm")
            } finally {
                csv.delete()
            }
        }, new VelocityTemplateEngine())
    }
}

