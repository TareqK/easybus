/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.processor;

/**
 *
 * @author tareq
 */
import com.google.auto.service.AutoService;
import java.util.List;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import me.kisoft.easybus.Event;
import me.kisoft.easybus.Handles;
import org.apache.commons.lang3.StringUtils;

@SupportedAnnotationTypes("*")
@AutoService(Processor.class)
public class HandlerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        ElementFilter.typesIn(roundEnv.getRootElements())
                .stream()
                .filter(this::hasHandlerAnnotation)
                .forEach(typeElement -> {
                    checkForHandlers(typeElement);
                    checkForEventClass(typeElement);
                });
        return false;
    }

    private void checkForEventClass(TypeElement typeElement) {
        if (getEventClass(typeElement.getAnnotation(Handles.class)) == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error in Class : +" + typeElement + " : No Event Class Specified.");
        }
        if (getEventClass(typeElement.getAnnotation(Handles.class)) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error in Class : +" + typeElement + " : Event Class Not annotated with @Event");
        }
    }

    private TypeMirror getEventClass(Handles handles) {
        try {
            handles.value(); 
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null; 
    }

    private void checkForHandlers(TypeElement typeElement) {
        List<ExecutableElement> methods
                = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        if (!methods.stream().anyMatch(
                m -> m.getParameters().size() == 1
                && StringUtils.equals(getEventClass(typeElement.getAnnotation(Handles.class
                )).toString(), m.getParameters().get(0).asType().toString()))) {
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR,
                            "Error in Class : +" + typeElement + " : handle method for Specified Event type not defined");
        }
    }

    private boolean hasHandlerAnnotation(TypeElement typeElement) {
        Handles handles = typeElement.getAnnotation(Handles.class
        );
        return handles != null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
