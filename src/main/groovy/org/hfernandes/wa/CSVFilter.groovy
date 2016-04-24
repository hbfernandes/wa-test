package org.hfernandes.wa

class CSVFilter{

    Map colIndex
    Map minMax
    File csv

    CSVFilter(File csv){
        this.csv = csv
        this.colIndex = [id: -1, decision: -1, vars: []]
        this.minMax = [:].withDefault { [min : Integer.MAX_VALUE, max: Integer.MIN_VALUE] }
    }

    /**
     * Build the column index with the header
     * @param headers
     */
    private void processHeader(List headers){
        headers.eachWithIndex{ String header, int i ->
            if(header.toLowerCase() in ['id', 'decision']){
                colIndex[header.toLowerCase()] = i
            }
            else{
                colIndex.vars << i
            }
        }
    }

    /**
     * Process a line for min/max variable values
     * @param line
     */
    private void processLine(List line){
        // Decision rows have value '1'
        if(line[colIndex.decision] == '1'){
            colIndex.vars.each { int vindex ->
                int value = line[vindex] as int
                minMax[vindex].min = Integer.min(minMax[vindex].min as int, value)
                minMax[vindex].max = Integer.max(minMax[vindex].max as int, value)
            }
        }
    }

    /**
     * Check if a line should be included in the result. Line is included if one is true:
     * - It's the header
     * - Decision column has value '1'
     * - At least one of the variables in within it's min/max range
     *
     * @param line
     * @param lnumber
     * @return
     */
    private boolean includeLine(List line, int lnumber){
        lnumber == 1 ||
        line[colIndex.decision] == '1' ||
        colIndex.vars.any { int vindex ->
            int value = line[vindex] as int
            minMax[vindex].min <= value && value <= minMax[vindex].max
        }
    }

    /**
     * Parse a line into a list of values
     * @param line
     * @return
     */
    private List parseLine(String line){
        line?.split(',')*.trim()
    }

    /**
     * Process the csv returning a list of filtered rows
     * @return
     */
    List process(){
        csv.eachLine { String line, int lnumber ->
            List parsedLine = parseLine(line)

            if (lnumber == 1) {
                processHeader(parsedLine)
                return
            }

            processLine(parsedLine)
        }

        List output = []
        csv.eachLine { String line, int lnumber ->
            List parsedLine = parseLine(line)
            if(includeLine(parsedLine, lnumber)){
                output << parsedLine
            }
        }

        output
    }


    static List filter(File csv){
        new CSVFilter(csv).process()
    }
}
