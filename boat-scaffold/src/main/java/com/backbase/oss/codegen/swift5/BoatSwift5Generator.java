package com.backbase.oss.codegen.swift5;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.openapitools.codegen.*;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.utils.CamelizeOption;
import org.openapitools.codegen.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class BoatSwift5Generator extends DefaultCodegen implements CodegenConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoatSwift5Generator.class);

    public static final String PROJECT_NAME = "projectName";
    public static final String RESPONSE_AS = "responseAs";
    public static final String DEPENDENCY_MANAGEMENT = "dependenciesAs";
    public  static final String OBJC_COMPATIBLE = "objcCompatible";
    public static final String POD_SOURCE = "podSource";
    public static final String POD_AUTHORS = "podAuthors";
    public static final String POD_SOCIAL_MEDIA_URL = "podSocialMediaURL";
    public static final String POD_LICENSE = "podLicense";
    public static final String POD_HOMEPAGE = "podHomepage";
    public static final String POD_SUMMARY = "podSummary";
    public static final String POD_DESCRIPTION = "podDescription";
    public static final String POD_SCREENSHOTS = "podScreenshots";
    public static final String POD_DOCUMENTATION_URL = "podDocumentationURL";
    public static final String SWIFT_USE_API_NAMESPACE = "swiftUseApiNamespace";
    public static final String DEFAULT_POD_AUTHORS = "BOAT Generator";
    public static final String LENIENT_TYPE_CAST = "lenientTypeCast";
    protected static final String LIBRARY_ALAMOFIRE = "alamofire";
    protected static final String LIBRARY_URLSESSION = "urlsession";
    protected static final String LIBRARY_DBS = "dbsDataProvider";
    protected static final String RESPONSE_LIBRARY_PROMISE_KIT = "PromiseKit";
    protected static final String RESPONSE_LIBRARY_RX_SWIFT = "RxSwift";
    protected static final String RESPONSE_LIBRARY_RESULT = "Result";
    protected static final String RESPONSE_LIBRARY_COMBINE = "Combine";
    protected static final String RESPONSE_LIBRARY_CALL = "Call";
    protected static final String[] RESPONSE_LIBRARIES = {RESPONSE_LIBRARY_PROMISE_KIT, RESPONSE_LIBRARY_RX_SWIFT, RESPONSE_LIBRARY_RESULT, RESPONSE_LIBRARY_COMBINE, RESPONSE_LIBRARY_CALL};
    protected static final String DEPENDENCY_MANAGEMENT_PODFILE = "Podfile";
    protected static final String DEPENDENCY_MANAGEMENT_CARTFILE = "Cartfile";
    protected static final String[] DEPENDENCY_MANAGEMENT_OPTIONS = {DEPENDENCY_MANAGEMENT_CARTFILE, DEPENDENCY_MANAGEMENT_PODFILE};
    protected String projectName = "OpenAPIClient";
    protected boolean nonPublicApi = false;
    protected boolean objcCompatible = false;
    protected boolean lenientTypeCast = false;
    protected boolean swiftUseApiNamespace;
    protected String[] responseAs = new String[0];
    protected String[] dependenciesAs = new String[0];
    protected String sourceFolder = "Classes" + File.separator + "OpenAPIs";
    protected HashSet objcReservedWords;
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    /**
     * Constructor for the Swift5 language codegen module
     */
    public BoatSwift5Generator() {
        super();

        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata).build();

        outputFolder = "generated-code" + File.separator + "swift";
        modelTemplateFiles.put("model.mustache", ".swift");
        apiTemplateFiles.put("api.mustache", ".swift");
        apiTemplateFiles.put("api_parameters.mustache", "RequestParams.swift");
        embeddedTemplateDir = templateDir = "swift5";
        apiPackage = File.separator + "APIs";
        modelPackage = File.separator + "Models";
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        languageSpecificPrimitives = new HashSet<>(
                Arrays.asList(
                        "Int",
                        "Int32",
                        "Int64",
                        "Float",
                        "Double",
                        "Bool",
                        "Void",
                        "String",
                        "URL",
                        "Data",
                        "Date",
                        "Character",
                        "UUID",
                        "URL",
                        "AnyObject",
                        "Any",
                        "Decimal")
        );
        defaultIncludes = new HashSet<>(
                Arrays.asList(
                        "Data",
                        "Date",
                        "URL", // for file
                        "UUID",
                        "Array",
                        "Dictionary",
                        "Set",
                        "Any",
                        "Empty",
                        "AnyObject",
                        "Any",
                        "Decimal")
        );
        objcReservedWords = new HashSet<>(
                Arrays.asList(
                        // Added for Objective-C compatibility
                        "id", "description", "NSArray", "NSURL", "CGFloat", "NSSet", "NSString", "NSInteger", "NSUInteger",
                        "NSError", "NSDictionary"
                )
        );
        reservedWords = new HashSet<>(
                Arrays.asList(
                        // name used by swift client
                        "ErrorResponse", "Response",

                        // Swift keywords. This list is taken from here:
                        // https://developer.apple.com/library/content/documentation/Swift/Conceptual/Swift_Programming_Language/LexicalStructure.html#//apple_ref/doc/uid/TP40014097-CH30-ID410
                        //
                        // Keywords used in declarations
                        "associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import", "init",
                        "inout", "internal", "let", "open", "operator", "private", "protocol", "public", "static", "struct",
                        "subscript", "typealias", "var",
                        // Keywords uses in statements
                        "break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for", "guard", "if",
                        "in", "repeat", "return", "switch", "where", "while",
                        // Keywords used in expressions and types
                        "as", "Any", "catch", "false", "is", "nil", "rethrows", "super", "self", "Self", "throw", "throws", "true", "try",
                        // Keywords used in patterns
                        "_",
                        // Keywords that begin with a number sign
                        "#available", "#colorLiteral", "#column", "#else", "#elseif", "#endif", "#file", "#fileLiteral", "#function", "#if",
                        "#imageLiteral", "#line", "#selector", "#sourceLocation",
                        // Keywords reserved in particular contexts
                        "associativity", "convenience", "dynamic", "didSet", "final", "get", "infix", "indirect", "lazy", "left",
                        "mutating", "none", "nonmutating", "optional", "override", "postfix", "precedence", "prefix", "Protocol",
                        "required", "right", "set", "Type", "unowned", "weak", "willSet",

                        //
                        // Swift Standard Library types
                        // https://developer.apple.com/documentation/swift
                        //
                        // Numbers and Basic Values
                        "Bool", "Int", "Double", "Float", "Range", "ClosedRange", "Error", "Optional",
                        // Special-Use Numeric Types
                        "UInt", "UInt8", "UInt16", "UInt32", "UInt64", "Int8", "Int16", "Int32", "Int64", "Float80", "Float32", "Float64",
                        // Strings and Text
                        "String", "Character", "Unicode", "StaticString",
                        // Collections
                        "Array", "Dictionary", "Set", "OptionSet", "CountableRange", "CountableClosedRange",

                        // The following are commonly-used Foundation types
                        "URL", "Data", "Codable", "Encodable", "Decodable",

                        // The following are other words we want to reserve
                        "Void", "AnyObject", "Class", "dynamicType", "COLUMN", "FILE", "FUNCTION", "LINE"
                )
        );

        typeMapping = new HashMap<>();
        typeMapping.put("array", "Array");
        typeMapping.put("List", "Array");
        typeMapping.put("map", "Dictionary");
        typeMapping.put("date", "Date");
        typeMapping.put("Date", "Date");
        typeMapping.put("DateTime", "Date");
        typeMapping.put("boolean", "Bool");
        typeMapping.put("string", "String");
        typeMapping.put("char", "Character");
        typeMapping.put("short", "Int");
        typeMapping.put("int", "Int");
        typeMapping.put("long", "Int64");
        typeMapping.put("integer", "Int");
        typeMapping.put("Integer", "Int");
        typeMapping.put("float", "Float");
        typeMapping.put("number", "Double");
        typeMapping.put("double", "Double");
        typeMapping.put("object", "Any");
        typeMapping.put("AnyType", "Any");
        typeMapping.put("file", "URL");
        typeMapping.put("binary", "Data");
        typeMapping.put("ByteArray", "Data");
        typeMapping.put("UUID", "UUID");
        typeMapping.put("URI", "String");
        typeMapping.put("BigDecimal", "Decimal");

        importMapping = new HashMap<>();

        cliOptions.add(new CliOption(PROJECT_NAME, "Project name in Xcode"));
        cliOptions.add(new CliOption(RESPONSE_AS,
                "Optionally use libraries to manage response.  Currently "
                        + StringUtils.join(RESPONSE_LIBRARIES, ", ")
                        + " are available."));
        cliOptions.add(new CliOption(DEPENDENCY_MANAGEMENT,
                "Available dependency managers "
                        + StringUtils.join(DEPENDENCY_MANAGEMENT_OPTIONS, ", ")
                        + " are available."));
        cliOptions.add(new CliOption(CodegenConstants.NON_PUBLIC_API,
                CodegenConstants.NON_PUBLIC_API_DESC
                        + "(default: false)"));
        cliOptions.add(new CliOption(OBJC_COMPATIBLE,
                "Add additional properties and methods for Objective-C "
                        + "compatibility (default: false)"));
        cliOptions.add(new CliOption(POD_SOURCE, "Source information used for Podspec"));
        cliOptions.add(new CliOption(CodegenConstants.POD_VERSION, "Version used for Podspec"));
        cliOptions.add(new CliOption(POD_AUTHORS, "Authors used for Podspec"));
        cliOptions.add(new CliOption(POD_SOCIAL_MEDIA_URL, "Social Media URL used for Podspec"));
        cliOptions.add(new CliOption(POD_LICENSE, "License used for Podspec"));
        cliOptions.add(new CliOption(POD_HOMEPAGE, "Homepage used for Podspec"));
        cliOptions.add(new CliOption(POD_SUMMARY, "Summary used for Podspec"));
        cliOptions.add(new CliOption(POD_DESCRIPTION, "Description used for Podspec"));
        cliOptions.add(new CliOption(POD_SCREENSHOTS, "Screenshots used for Podspec"));
        cliOptions.add(new CliOption(POD_DOCUMENTATION_URL,
                "Documentation URL used for Podspec"));
        cliOptions.add(new CliOption(SWIFT_USE_API_NAMESPACE,
                "Flag to make all the API classes inner-class "
                        + "of {{projectName}}API"));
        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP,
                CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(LENIENT_TYPE_CAST,
                "Accept and cast values for simple types (string->bool, "
                        + "string->int, int->string)")
                .defaultValue(Boolean.FALSE.toString()));

        supportedLibraries.put(LIBRARY_URLSESSION, "[DEFAULT] HTTP client: URLSession");
        supportedLibraries.put(LIBRARY_ALAMOFIRE, "HTTP client: Alamofire");
        supportedLibraries.put(LIBRARY_DBS, "HTTP client: DBSDataProvider");

        CliOption libraryOption = new CliOption(CodegenConstants.LIBRARY, "Library template (sub-template) to use");
        libraryOption.setEnum(supportedLibraries);
        libraryOption.setDefault(LIBRARY_URLSESSION);
        cliOptions.add(libraryOption);
        setLibrary(LIBRARY_DBS);
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }
    @Override
    public String getName() {
        return "boat-swift5";
    }
    @Override
    public String getHelp() {
        return "Generates a Swift 5.x client library";
    }
    @Override
    protected void addAdditionPropertiesToCodeGenModel(CodegenModel codegenModel, Schema schema) {
        codegenModel.additionalPropertiesType = getTypeDeclaration(ModelUtils.getAdditionalProperties(openAPI, schema));
        addImport(codegenModel, codegenModel.additionalPropertiesType);
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (StringUtils.isEmpty(System.getenv("SWIFT_POST_PROCESS_FILE"))) {
            LOGGER.info("Environment variable SWIFT_POST_PROCESS_FILE not defined so the Swift code may not be properly formatted. To define it, try 'export SWIFT_POST_PROCESS_FILE=/usr/local/bin/swiftformat' (Linux/Mac)");
            LOGGER.info("NOTE: To enable file post-processing, 'enablePostProcessFile' must be set to `true` (--enable-post-process-file for CLI).");
        }
        // Setup project name
        if (additionalProperties.containsKey(PROJECT_NAME)) {
            setProjectName((String) additionalProperties.get(PROJECT_NAME));
        } else {
            additionalProperties.put(PROJECT_NAME, projectName);
        }
        sourceFolder = projectName + File.separator + sourceFolder;

        // Setup nonPublicApi option, which generates code with reduced access
        // modifiers; allows embedding elsewhere without exposing non-public API calls
        // to consumers
        if (additionalProperties.containsKey(CodegenConstants.NON_PUBLIC_API)) {
            setNonPublicApi(convertPropertyToBooleanAndWriteBack(CodegenConstants.NON_PUBLIC_API));
        }
        additionalProperties.put(CodegenConstants.NON_PUBLIC_API, nonPublicApi);

        // Setup objcCompatible option, which adds additional properties
        // and methods for Objective-C compatibility
        if (additionalProperties.containsKey(OBJC_COMPATIBLE)) {
            setObjcCompatible(convertPropertyToBooleanAndWriteBack(OBJC_COMPATIBLE));
        }
        additionalProperties.put(OBJC_COMPATIBLE, objcCompatible);
        // add objc reserved words
        if (Boolean.TRUE.equals(objcCompatible)) {
            reservedWords.addAll(objcReservedWords);
        }

        if (additionalProperties.containsKey(RESPONSE_AS)) {
            Object responseAsObject = additionalProperties.get(RESPONSE_AS);
            if (responseAsObject instanceof String) {
                setResponseAs(((String) responseAsObject).split(","));
            } else {
                setResponseAs((String[]) responseAsObject);
            }
        }
        additionalProperties.put(RESPONSE_AS, responseAs);
        if (ArrayUtils.contains(responseAs, RESPONSE_LIBRARY_PROMISE_KIT)) {
            additionalProperties.put("usePromiseKit", true);
        }
        if (ArrayUtils.contains(responseAs, RESPONSE_LIBRARY_RX_SWIFT)) {
            additionalProperties.put("useRxSwift", true);
        }
        if (ArrayUtils.contains(responseAs, RESPONSE_LIBRARY_RESULT)) {
            additionalProperties.put("useResult", true);
        }
        if (ArrayUtils.contains(responseAs, RESPONSE_LIBRARY_COMBINE)) {
            additionalProperties.put("useCombine", true);
        }
        if (ArrayUtils.contains(responseAs, RESPONSE_LIBRARY_CALL)) {
            additionalProperties.put("useCall", true);
        }

        // Setup swiftUseApiNamespace option, which makes all the API
        // classes inner-class of {{projectName}}API
        if (additionalProperties.containsKey(SWIFT_USE_API_NAMESPACE)) {
            setSwiftUseApiNamespace(convertPropertyToBooleanAndWriteBack(SWIFT_USE_API_NAMESPACE));
        }

        if (!additionalProperties.containsKey(POD_AUTHORS)) {
            additionalProperties.put(POD_AUTHORS, DEFAULT_POD_AUTHORS);
        }

        setLenientTypeCast(convertPropertyToBooleanAndWriteBack(LENIENT_TYPE_CAST));

        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        supportingFiles.add(new SupportingFile("Podspec.mustache",
                "",
                projectName + ".podspec"));

        if (additionalProperties.containsKey(DEPENDENCY_MANAGEMENT)) {
            Object dependenciesAsObject = additionalProperties.get(DEPENDENCY_MANAGEMENT);
            if (dependenciesAsObject instanceof String) {
                setDependenciesAs(((String) dependenciesAsObject).split(","));
            } else {
                setDependenciesAs((String[]) dependenciesAsObject);
            }
        }
        additionalProperties.put(DEPENDENCY_MANAGEMENT, dependenciesAs);
        if (ArrayUtils.contains(dependenciesAs, DEPENDENCY_MANAGEMENT_PODFILE)) {
            supportingFiles.add(new SupportingFile("Podfile.mustache",
                    "",
                    "Podfile"));
        }
        if (ArrayUtils.contains(dependenciesAs, DEPENDENCY_MANAGEMENT_CARTFILE)) {
            supportingFiles.add(new SupportingFile("Cartfile.mustache",
                    "",
                    "Cartfile"));
        }

        supportingFiles.add(new SupportingFile("Package.swift.mustache",
                "",
                "Package.swift"));
        supportingFiles.add(new SupportingFile("APIHelper.mustache",
                sourceFolder,
                "APIHelper.swift"));
        supportingFiles.add(new SupportingFile("Configuration.mustache",
                sourceFolder,
                "Configuration.swift"));
        supportingFiles.add(new SupportingFile("Extensions.mustache",
                sourceFolder,
                "Extensions.swift"));
        supportingFiles.add(new SupportingFile("Models.mustache",
                sourceFolder,
                "Models.swift"));
        supportingFiles.add(new SupportingFile("CodableHelper.mustache",
                sourceFolder,
                "CodableHelper.swift"));
        supportingFiles.add(new SupportingFile("OpenISO8601DateFormatter.mustache",
                sourceFolder,
                "OpenISO8601DateFormatter.swift"));
        supportingFiles.add(new SupportingFile("SynchronizedDictionary.mustache",
                sourceFolder,
                "SynchronizedDictionary.swift"));
        supportingFiles.add(new SupportingFile("gitignore.mustache",
                "",
                ".gitignore"));
        supportingFiles.add(new SupportingFile("README.mustache",
                "",
                "README.md"));
        supportingFiles.add(new SupportingFile("XcodeGen.mustache",
                "",
                "project.yml"));
        supportingFiles.add(new SupportingFile("AnyCodable.swift.mustache",
                sourceFolder,
                "AnyCodable.swift"));

        switch (getLibrary()) {
            case LIBRARY_ALAMOFIRE:
                additionalProperties.put("useAlamofire", true);
                supportingFiles.add(new SupportingFile("AlamofireImplementations.mustache",
                        sourceFolder,
                        "AlamofireImplementations.swift"));
                break;
            case LIBRARY_URLSESSION:
                additionalProperties.put("useURLSession", true);
                supportingFiles.add(new SupportingFile("URLSessionImplementations.mustache",
                        sourceFolder,
                        "URLSessionImplementations.swift"));
                break;
            case LIBRARY_DBS:
                additionalProperties.put("useDBSDataProvider", true);
            default:
                break;
        }

    }

    @Override
    protected boolean isReservedWord(String word) {
        return word != null && reservedWords.contains(word); // don't lowercase as super does
    }
    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;  // add an underscore to the name
    }
    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder
                + modelPackage().replace('.', File.separatorChar);
    }
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder
                + apiPackage().replace('.', File.separatorChar);
    }
    @Override
    public String getTypeDeclaration(Schema schema) {
        if (ModelUtils.isArraySchema(schema)) {
            ArraySchema ap = (ArraySchema) schema;
            Schema inner = ap.getItems();
            return "[" + getTypeDeclaration(inner) + "]";
        } else if (ModelUtils.isMapSchema(schema)) {
            Schema inner = ModelUtils.getAdditionalProperties(openAPI, schema);
            return "[String:" + getTypeDeclaration(inner) + "]";
        }
        return super.getTypeDeclaration(schema);
    }
    @Override
    public boolean isDataTypeFile(String dataType) {
        return dataType != null && dataType.equals("URL");
    }
    @Override
    public boolean isDataTypeBinary(final String dataType) {
        return dataType != null && dataType.equals("Data");
    }

    /**
     * Output the proper model name (capitalized).
     *
     * @param name the name of the model
     * @return capitalized model name
     */
    @Override
    public String toModelName(String name) {
        // FIXME parameter should not be assigned. Also declare it as "final"
        name = sanitizeName(name);

        if (!StringUtils.isEmpty(modelNameSuffix)) { // set model suffix
            name = name + "_" + modelNameSuffix;
        }

        if (!StringUtils.isEmpty(modelNamePrefix)) { // set model prefix
            name = modelNamePrefix + "_" + name;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        name = camelize(name);

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            String modelName = "Model" + name;
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to "
                    + modelName);
            return modelName;
        }
        // model name starts with number
        if (name.matches("^\\d.*")) {
            // e.g. 200Response => Model200Response (after camelize)
            String modelName = "Model" + name;
            LOGGER.warn(name
                    + " (model name starts with number) cannot be used as model name."
                    + " Renamed to " + modelName);
            return modelName;
        }

        return name;
    }

    /**
     * Return the capitalized file name of the model.
     *
     * @param name the model name
     * @return the file name of the model
     */
    @Override
    public String toModelFilename(String name) {
        return toModelName(name);
    }

    @Override
    public String toDefaultValue(Schema schema) {
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            if (schema.getDefault() != null) {
                if (ModelUtils.isStringSchema(schema)) {
                    if (hasEnumVarnames(schema)) {
                        return "." + (String) schema.getDefault();
                    } else {
                        return "." + toEnumVarName(escapeText((String) schema.getDefault()), schema.getType());
                    }
                } else {
                    if (hasEnumVarnames(schema)) {
                        return "." + schema.getDefault().toString();
                    }  else {
                        return "." + toEnumVarName(escapeText(schema.getDefault().toString()), schema.getType());
                    }
                }
            }
        }
        if (ModelUtils.isIntegerSchema(schema) || ModelUtils.isNumberSchema(schema) || ModelUtils.isBooleanSchema(schema)) {
            if (schema.getDefault() != null) {
                return schema.getDefault().toString();
            }
        } else if (ModelUtils.isStringSchema(schema)) {
            if (schema.getDefault() != null) {
                return "\"" + escapeText((String) schema.getDefault()) + "\"";
            }
        }
        return null;
    }
    @Override
    public String toInstantiationType(Schema schema) {
        if (ModelUtils.isMapSchema(schema)) {
            return getSchemaType(ModelUtils.getAdditionalProperties(openAPI, schema));
        } else if (ModelUtils.isArraySchema(schema)) {
            ArraySchema ap = (ArraySchema) schema;
            String inner = getSchemaType(ap.getItems());
            return "[" + inner + "]";
        }
        return null;
    }
    @Override
    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultAPI";
        }
        return camelize(name) + "API";
    }
    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace("/", File.separator);
    }
    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace("/", File.separator);
    }
    @Override
    public String toModelDocFilename(String name) {
        return toModelName(name);
    }
    @Override
    public String toApiDocFilename(String name) {
        return toApiName(name);
    }
    @Override
    public String toOperationId(String operationId) {
        operationId = camelize(sanitizeName(operationId), CamelizeOption.LOWERCASE_FIRST_CHAR);

        // Throw exception if method name is empty.
        // This should not happen but keep the check just in case
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = camelize(("call_" + operationId), CamelizeOption.LOWERCASE_FIRST_CHAR);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name."
                    + " Renamed to " + newOperationId);
            return newOperationId;
        }

        // operationId starts with a number
        if (operationId.matches("^\\d.*")) {
            LOGGER.warn(operationId + " (starting with a number) cannot be used as method name. Renamed to " + camelize(sanitizeName("call_" + operationId)), CamelizeOption.LOWERCASE_FIRST_CHAR);
            operationId = camelize(sanitizeName("call_" + operationId), CamelizeOption.LOWERCASE_FIRST_CHAR);
        }

        return operationId;
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name);

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize the variable name
        // pet_id => petId
        name = camelize(name, CamelizeOption.LOWERCASE_FIRST_CHAR);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }
    @Override
    public String toParamName(String name) {
        // sanitize name
        name = sanitizeName(name);

        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_");

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize(lower) the variable name
        // pet_id => petId
        name = camelize(name, CamelizeOption.LOWERCASE_FIRST_CHAR);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public CodegenModel fromModel(String name, Schema schema) {
        Map<String, Schema> allDefinitions = ModelUtils.getSchemas(this.openAPI);
        CodegenModel codegenModel = super.fromModel(name, schema);
        if (codegenModel.description != null) {
            codegenModel.imports.add("ApiModel");
        }

        fixAllFreeFormObject(codegenModel);

        return codegenModel;
    }

    private void fixAllFreeFormObject(CodegenModel codegenModel) {
        this.fixFreeFormObject(codegenModel.vars);
        this.fixFreeFormObject(codegenModel.optionalVars);
        this.fixFreeFormObject(codegenModel.requiredVars);
        this.fixFreeFormObject(codegenModel.parentVars);
        this.fixFreeFormObject(codegenModel.allVars);
        this.fixFreeFormObject(codegenModel.readOnlyVars);
        this.fixFreeFormObject(codegenModel.readWriteVars);
    }

    /*
    If a property has both isFreeFormObject and isMapContainer true make isFreeFormObject false
    This way when we have a free form object in the spec that has a typed value it will be
    treated as a Dictionary
   */
    private void fixFreeFormObject(List<CodegenProperty> codegenProperties) {
        Iterator<CodegenProperty> iterator = codegenProperties.iterator();
        while (iterator.hasNext()) {
            CodegenProperty codegenProperty = iterator.next();
//            if (codegenProperty.isFreeFormObject && codegenProperty.isMapContainer && codegenProperty.items.isFreeFormObject == false) {
//            MISSING: isMapContainer
            if (codegenProperty.isFreeFormObject && !codegenProperty.items.isFreeFormObject) {
                codegenProperty.isFreeFormObject = false;
            }
//            if (codegenProperty.isMapContainer && codegenProperty.items.isFreeFormObject) {
//            MISSING: isMapContainer
            if (codegenProperty.items.isFreeFormObject) {
                codegenProperty.isFreeFormObject = true;
            }
        }
    }

    @Override
    public String getSchemaType(Schema schema) {
        String openAPIType = super.getSchemaType(schema);
        String type;
        if (typeMapping.containsKey(openAPIType)) {
            type = typeMapping.get(openAPIType);
            if (languageSpecificPrimitives.contains(type) || defaultIncludes.contains(type)) {
                return type;
            }
        } else {
            type = openAPIType;
        }
        return toModelName(type);
    }

    public void setNonPublicApi(boolean nonPublicApi) {
        this.nonPublicApi = nonPublicApi;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public void setObjcCompatible(boolean objcCompatible) {
        this.objcCompatible = objcCompatible;
    }
    public void setResponseAs(String[] responseAs) {
        this.responseAs = responseAs;
    }
    public void setSwiftUseApiNamespace(boolean swiftUseApiNamespace) {
        this.swiftUseApiNamespace = swiftUseApiNamespace;
    }
    public void setLenientTypeCast(boolean lenientTypeCast) {
        this.lenientTypeCast = lenientTypeCast;
    }
    public void setDependenciesAs(String[] dependenciesAs) {
        this.dependenciesAs = dependenciesAs;
    }

    private boolean hasEnumVarnames(Schema p) {
        Map<String, Object> extensions = p.getExtensions();
        if (extensions == null) {
            return false;
        } else {
            return extensions.containsKey("x-enum-varnames");
        }
    }
    @Override
    public String toEnumDefaultValue(String value, String datatype) {
        return datatype + "_" + value;
    }
    @Override
    public String toEnumVarName(String name, String datatype) {
        if (name.isEmpty()) {
            return "empty";
        }

        Pattern startWithNumberPattern = Pattern.compile("^\\d+");
        Matcher startWithNumberMatcher = startWithNumberPattern.matcher(name);
        if (startWithNumberMatcher.find()) {
            String startingNumbers = startWithNumberMatcher.group(0);
            String nameWithoutStartingNumbers = name.substring(startingNumbers.length());

            return "_" + startingNumbers + camelize(nameWithoutStartingNumbers, CamelizeOption.LOWERCASE_FIRST_CHAR);
        }

        // for symbol, e.g. $, #
        if (getSymbolName(name) != null) {
            return camelize(WordUtils.capitalizeFully(getSymbolName(name).toUpperCase(Locale.ROOT)), CamelizeOption.LOWERCASE_FIRST_CHAR);
        }

        // Camelize only when we have a structure defined below
        Boolean camelized = false;
        if (name.matches("[A-Z][a-z0-9]+[a-zA-Z0-9]*")) {
            name = camelize(name, CamelizeOption.LOWERCASE_FIRST_CHAR);
            camelized = true;
        }

        // Reserved Name
        String nameLowercase = StringUtils.lowerCase(name);
        if (isReservedWord(nameLowercase)) {
            return escapeReservedWord(nameLowercase);
        }

        // Check for numerical conversions
        if ("Int".equals(datatype) || "Int32".equals(datatype) || "Int64".equals(datatype)
                || "Float".equals(datatype) || "Double".equals(datatype)) {
            String varName = "number" + camelize(name);
            varName = varName.replaceAll("-", "minus");
            varName = varName.replaceAll("\\+", "plus");
            varName = varName.replaceAll("\\.", "dot");
            return varName;
        }

        // If we have already camelized the word, don't progress
        // any further
        if (camelized) {
            return name;
        }

        char[] separators = {'-', '_', ' ', ':', '(', ')'};
        return camelize(WordUtils.capitalizeFully(StringUtils.lowerCase(name), separators)
                        .replaceAll("[-_ :\\(\\)]", ""), CamelizeOption.LOWERCASE_FIRST_CHAR);
    }
    @Override
    public String toEnumName(CodegenProperty property) {
        String enumName = toModelName(property.name);

        // Ensure that the enum type doesn't match a reserved word or
        // the variable name doesn't match the generated enum type or the
        // Swift compiler will generate an error
        if (isReservedWord(property.datatypeWithEnum)
                || toVarName(property.name).equals(property.datatypeWithEnum)) {
            enumName = property.datatypeWithEnum + "Enum";
        }

        // TODO: toModelName already does something for names starting with number,
        // so this code is probably never called
        if (enumName.matches("\\d.*")) { // starts with number
            return "_" + enumName;
        } else {
            return enumName;
        }
    }

//    @Override
//    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
//        Map<String, Object> postProcessedModelsEnum = postProcessModelsEnum(objs);
//
//        // We iterate through the list of models, and also iterate through each of the
//        // properties for each model. For each property, if:
//        //
//        // CodegenProperty.name != CodegenProperty.baseName
//        //
//        // then we set
//        //
//        // CodegenProperty.vendorExtensions["x-codegen-escaped-property-name"] = true
//        //
//        // Also, if any property in the model has x-codegen-escaped-property-name=true, then we mark:
//        //
//        // CodegenModel.vendorExtensions["x-codegen-has-escaped-property-names"] = true
//        //
//        List<Object> models = (List<Object>) postProcessedModelsEnum.get("models");
//        for (Object _mo : models) {
//            Map<String, Object> mo = (Map<String, Object>) _mo;
//            CodegenModel cm = (CodegenModel) mo.get("model");
//            boolean modelHasPropertyWithEscapedName = false;
//            for (CodegenProperty prop : cm.allVars) {
//                if (!prop.name.equals(prop.baseName)) {
//                    prop.vendorExtensions.put("x-codegen-escaped-property-name", true);
//                    modelHasPropertyWithEscapedName = true;
//                }
//            }
//            if (modelHasPropertyWithEscapedName) {
//                cm.vendorExtensions.put("x-codegen-has-escaped-property-names", true);
//            }
//        }
//
//        return postProcessedModelsEnum;
//    }

//    /*
//     Iterate over all models to call fixInheritance
//    */
//    @Override
//    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
//        Map<String, Object> postProcessedModels = super.postProcessAllModels(objs);
//
//        Iterator it = postProcessedModels.entrySet().iterator();
//        while (it.hasNext()) {
//            Map<String, Object> model = (Map<String, Object>) ((Map.Entry)it.next()).getValue();
//            List<Object> models = (List<Object>) model.get("models");
//            for (Object _mo : models) {
//                Map<String, Object> mo = (Map<String, Object>) _mo;
//                CodegenModel cm = (CodegenModel) mo.get("model");
//                fixInheritance(cm);
//            }
//        }
//
//        return postProcessedModels;
//    }

    /*
    There's no inheritance for Swift structs, we're adding all parent vars
    recursively to the models allVars list while making sure we don't have duplicates.
   */
    private void fixInheritance(CodegenModel codegenModel) {
        CodegenModel parentModel = codegenModel.parentModel;

        while (parentModel != null) {
            if (!parentModel.vars.isEmpty()) {
                codegenModel.allVars.addAll(parentModel.vars);
                codegenModel.requiredVars.addAll(parentModel.requiredVars);
            }
            parentModel = parentModel.parentModel;
        }

        codegenModel.removeAllDuplicatedProperty();
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        boolean isSwiftScalarType = property.isInteger || property.isLong || property.isFloat
                || property.isDouble || property.isBoolean;
        if ((!property.required || property.isNullable) && isSwiftScalarType) {
            // Optional scalar types like Int?, Int64?, Float?, Double?, and Bool?
            // do not translate to Objective-C. So we want to flag those
            // properties in case we want to put special code in the templates
            // which provide Objective-C compatibility.
            property.vendorExtensions.put("x-swift-optional-scalar", true);
        }
    }
    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }
    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }
    @Override
    public void postProcessFile(File file, String fileType) {
        if (file == null) {
            return;
        }
        String swiftPostProcessFile = System.getenv("SWIFT_POST_PROCESS_FILE");
        if (StringUtils.isEmpty(swiftPostProcessFile)) {
            return; // skip if SWIFT_POST_PROCESS_FILE env variable is not defined
        }
        // only process files with swift extension
        if ("swift".equals(FilenameUtils.getExtension(file.toString()))) {
            String command = swiftPostProcessFile + " " + file.toString();
            try {
                Process p = Runtime.getRuntime().exec(command);
                int exitValue = p.waitFor();
                if (exitValue != 0) {
                    LOGGER.error("Error running the command ({}). Exit value: {}", command, exitValue);
                } else {
                    LOGGER.info("Successfully executed: " + command);
                }
            } catch (Exception e) {
                LOGGER.error("Error running the command ({}). Exception: {}", command, e.getMessage());
            }
        }
    }

//    @Override
//    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
//        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
//
//        HashMap<String, CodegenModel> modelMaps = new HashMap<String, CodegenModel>();
//        for (Object o : allModels) {
//            HashMap<String, Object> h = (HashMap<String, Object>) o;
//            CodegenModel m = (CodegenModel) h.get("model");
//            modelMaps.put(m.classname, m);
//        }
//
//        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
//        for (CodegenOperation operation : operations) {
//            for (CodegenParameter cp : operation.allParams) {
//                cp.vendorExtensions.put("x-swift-example", constructExampleCode(cp, modelMaps));
//            }
//        }
//        return objs;
//    }

//    public String constructExampleCode(CodegenParameter codegenParameter, HashMap<String, CodegenModel> modelMaps) {
//        if (codegenParameter.isListContainer) { // array
//            return "[" + constructExampleCode(codegenParameter.items, modelMaps) + "]";
//        } else if (codegenParameter.isMapContainer) { // TODO: map, file type
//            return "\"TODO\"";
//        } else if (languageSpecificPrimitives.contains(codegenParameter.dataType)) { // primitive type
//            if ("String".equals(codegenParameter.dataType) || "Character".equals(codegenParameter.dataType)) {
//                if (StringUtils.isEmpty(codegenParameter.example)) {
//                    return "\"" + codegenParameter.example + "\"";
//                } else {
//                    return "\"" + codegenParameter.paramName + "_example\"";
//                }
//            } else if ("Bool".equals(codegenParameter.dataType)) { // boolean
//                if (Boolean.parseBoolean(codegenParameter.example)) {
//                    return "true";
//                } else {
//                    return "false";
//                }
//            } else if ("URL".equals(codegenParameter.dataType)) { // URL
//                return "URL(string: \"https://example.com\")!";
//            } else if ("Date".equals(codegenParameter.dataType)) { // date
//                return "Date()";
//            } else { // numeric
//                if (StringUtils.isEmpty(codegenParameter.example)) {
//                    return codegenParameter.example;
//                } else {
//                    return "987";
//                }
//            }
//        } else { // model
//            // look up the model
//            if (modelMaps.containsKey(codegenParameter.dataType)) {
//                return constructExampleCode(modelMaps.get(codegenParameter.dataType), modelMaps);
//            } else {
//                //LOGGER.error("Error in constructing examples. Failed to look up the model " + codegenParameter.dataType);
//                return "TODO";
//            }
//        }
//    }

//    public String constructExampleCode(CodegenProperty codegenProperty, HashMap<String, CodegenModel> modelMaps) {
//        if (codegenProperty.isListContainer) { // array
//            return "[" + constructExampleCode(codegenProperty.items, modelMaps) + "]";
//        } else if (codegenProperty.isMapContainer) { // TODO: map, file type
//            return "\"TODO\"";
//        } else if (languageSpecificPrimitives.contains(codegenProperty.dataType)) { // primitive type
//            if ("String".equals(codegenProperty.dataType) || "Character".equals(codegenProperty.dataType)) {
//                if (StringUtils.isEmpty(codegenProperty.example)) {
//                    return "\"" + codegenProperty.example + "\"";
//                } else {
//                    return "\"" + codegenProperty.name + "_example\"";
//                }
//            } else if ("Bool".equals(codegenProperty.dataType)) { // boolean
//                if (Boolean.parseBoolean(codegenProperty.example)) {
//                    return "true";
//                } else {
//                    return "false";
//                }
//            } else if ("URL".equals(codegenProperty.dataType)) { // URL
//                return "URL(string: \"https://example.com\")!";
//            } else if ("Date".equals(codegenProperty.dataType)) { // date
//                return "Date()";
//            } else { // numeric
//                if (StringUtils.isEmpty(codegenProperty.example)) {
//                    return codegenProperty.example;
//                } else {
//                    return "123";
//                }
//            }
//        } else {
//            // look up the model
//            if (modelMaps.containsKey(codegenProperty.dataType)) {
//                return constructExampleCode(modelMaps.get(codegenProperty.dataType), modelMaps);
//            } else {
//                //LOGGER.error("Error in constructing examples. Failed to look up the model " + codegenProperty.dataType);
//                return "\"TODO\"";
//            }
//        }
//    }

//    public String constructExampleCode(CodegenModel codegenModel, HashMap<String, CodegenModel> modelMaps) {
//        String example;
//        example = codegenModel.name + "(";
//        List<String> propertyExamples = new ArrayList<>();
//        for (CodegenProperty codegenProperty : codegenModel.vars) {
//            propertyExamples.add(codegenProperty.name + ": " + constructExampleCode(codegenProperty, modelMaps));
//        }
//        example += StringUtils.join(propertyExamples, ", ");
//        example += ")";
//        return example;
//    }


}
