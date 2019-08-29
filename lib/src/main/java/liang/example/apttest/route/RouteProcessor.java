package liang.example.apttest.route;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class RouteProcessor extends AbstractProcessor {
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add(Route.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        HashMap<String, String> nameMap = new HashMap<>();
        Set<? extends Element> annotationElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        for (Element element : annotationElements) {
            Route route = element.getAnnotation(Route.class);
            nameMap.put(route.path(), element.getSimpleName().toString());
        }
        generateJavaFile(nameMap);
        return true;
    }

    private void generateJavaFile(Map<String, String> nameMap) {
        // constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("routeMap = new $T<>()", HashMap.class);
        for (Map.Entry<String, String> entry : nameMap.entrySet()) {
            constructorBuilder.addStatement("routeMap.put(\"$N\", \"$N\")", entry.getKey(), entry.getValue());
        }
        MethodSpec constructor = constructorBuilder.build();
        // getActivityName
        MethodSpec getActivityName = MethodSpec.methodBuilder("getActivityName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "routeName")
                .beginControlFlow("if (routeMap != null && !routeMap.isEmpty())")
                .addStatement("return (String) routeMap.get(routeName)")
                .endControlFlow()
                .addStatement("return \"\"")
                .build();
        // class
        TypeSpec typeSpec = TypeSpec.classBuilder("Route$Finder")
                .addModifiers(Modifier.PUBLIC)
                // .addSuperinterface(Provider.class)
                .addField(ParameterizedTypeName.get(HashMap.class, String.class, String.class), "routeMap", Modifier.PRIVATE)
                .addMethod(constructor)
                .addMethod(getActivityName)
                .build();
        // final generation
        try {
            JavaFile javaFile = JavaFile.builder("liang.example.apttest.route", typeSpec).build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
