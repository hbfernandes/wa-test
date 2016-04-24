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
     * - At least one of the variables in this line is within it's min/max range
     */
    private boolean includeLine(List line, int lnumber){
        lnumber == 1 ||
        line[colIndex.decision] == '1' ||
        colIndex.vars.any { int vindex ->
            int value = line[vindex] as int
            minMax[vindex].min <= value && value <= minMax[vindex].max
        }
    }

    private List parseLine(String line){
        line?.split(',')*.trim()
    }

    private void process(){
        csv.eachLine { String line, int lnumber ->
            List parsedLine = parseLine(line)

            if (lnumber == 1) {
                processHeader(parsedLine)
                return
            }

            processLine(parsedLine)
        }
    }

    private List result(){
        List output = []
        csv.eachLine { String line, int lnumber ->
            List parsedLine = parseLine(line)
            if(includeLine(parsedLine, lnumber)){
                output << parsedLine
            }
        }

        output
    }

    /**
     * Filter the records of the given CSV file removing
     * the ones that do not match the criteria.
     * @param csv   numeric csv file with 'id', 'decision' and 'var' headers
     * @return      list of data rows for rendering
     */
    static List filter(File csv){
        CSVFilter filter = new CSVFilter(csv)
        filter.process()
        filter.result()
    }
}
