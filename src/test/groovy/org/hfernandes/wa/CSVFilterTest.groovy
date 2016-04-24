package org.hfernandes.wa

import spock.lang.Specification
import spock.lang.Unroll

class CSVFilterTest extends Specification{

    static File inputDir = new File('src/test/resources/input')
    static File outputDir = new File('src/test/resources/output')

    def "Process Header"(){
        setup:
        CSVFilter csvFilter = new CSVFilter(null)

        when:
        csvFilter.processHeader(header)

        then:
        csvFilter.colIndex == result

        where:
        header                              || result
        ['Id', 'Decision', 'Var1', 'var2']  || [id: 0, decision: 1, vars: [2, 3]]
        ['Id', 'Var1', 'Decision', 'Var2']  || [id: 0, decision: 2, vars: [1, 3]]
    }

    def "Process Line"(){
        setup:
        CSVFilter csvFilter = new CSVFilter(null)
        csvFilter.colIndex = [id: 0, decision: 1, vars: [2, 3]]
        csvFilter.minMax << minMax

        when:
        csvFilter.processLine(line)

        then:
        csvFilter.minMax == result

        where:
        minMax                  | line                   || result
        [2: [min: 10, max: 15]] | ['1', '1', '9', '1']   || [2: [min: 9, max: 15], 3: [min: 1, max: 1]]
        [3: [min: 0, max: 15]]  | ['2', '1', '-2', '20'] || [2: [min: -2, max: -2], 3: [min: 0, max: 20]]
    }

    def "Include Line"(){
        setup:
        CSVFilter csvFilter = new CSVFilter(null)
        csvFilter.colIndex = [id: 0, decision: 1, vars: [2, 3]]
        csvFilter.minMax = [2: [min: 0, max: 10], 3: [min: 5, max: 15]]

        expect:
        csvFilter.includeLine(line, lnumber) == result

        where:
        line                    | lnumber   || result
        []                      | 1         || true
        ['2', '1', '-2', '20']  | 2         || true
        ['2', '0', '-2', '20']  | 3         || false
        ['2', '0', '2', '20']   | 4         || true
    }

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
