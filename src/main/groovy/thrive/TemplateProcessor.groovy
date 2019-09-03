package thrive

import freemarker.template.Configuration
import org.apache.commons.io.FileUtils
import org.apache.tools.ant.DirectoryScanner

import static freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER

class TemplateProcessor {
    private static final String TEMPLATE_EXTENSION = '.ftl'
    private String templatesDir
    private String outputDir
    private Configuration cfg

    TemplateProcessor(String templatesDir, String outputDir) {
        this.templatesDir = templatesDir
        this.outputDir = outputDir

        cfg = new Configuration()
        cfg.setDirectoryForTemplateLoading(new File(templatesDir))
        cfg.setDefaultEncoding("UTF-8")
        cfg.setTemplateExceptionHandler(RETHROW_HANDLER)
    }

    void execute(Map properties) {
        DirectoryScanner scanner = createScanner()
        scanner.scan()
        scanner.includedFiles.each { String fileName ->
            if (fileName.endsWith(TEMPLATE_EXTENSION)) {
                process(fileName, properties)
            } else {
                copy(fileName)
            }
        }
    }

    private process(String fileName, Map properties) {
        def outputFile = new File(outputDir, fileName - TEMPLATE_EXTENSION)
        def template = cfg.getTemplate(fileName)

        outputFile.withWriter { out ->
            template.process(properties, out)
        }
    }

    private copy(String fileName) {
        def inputFile = new File(templatesDir, fileName)
        def outputFile = new File(outputDir, fileName)
        FileUtils.copyFile(inputFile, outputFile)
    }

    private DirectoryScanner createScanner() {
        def scanner = new DirectoryScanner()
        scanner.includes = ['**/*']
        scanner.basedir = new File(templatesDir)
        scanner
    }
}
