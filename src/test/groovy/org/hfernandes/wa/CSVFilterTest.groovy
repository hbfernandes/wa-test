package org.hfernandes.wa

import spock.lang.Specification
import spock.lang.Unroll

class CSVFilterTest extends Specification{

    static File inputDir = new File('src/test/resources/input')
    static File outputDir = new File('src/test/resources/output')

    @Unroll("Process file: #filename")
    def "Filter CSV"(){
        setup:
        File input = new File(inputDir, filename as String)
        File output = new File(outputDir, filename as String)
        List expected = []
        output.eachLine { String line ->
            expected << line.split(',')*.trim()
        }

        when:
        List result = CSVFilter.filter(input)

        then:
        result*.join(',').join('\n') == expected*.join(',').join('\n')

        where:
        filename << inputDir.list()
    }

}
