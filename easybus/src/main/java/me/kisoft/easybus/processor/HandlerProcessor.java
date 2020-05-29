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
import org.apache.commons.lang3.StringUtils;
import me.kisoft.easybus.Handle;

@SupportedAnnotationTypes("*")
@AutoService(Processor.class)
public class HandlerProcessor extends AbstractProcessor {

    private static final String NO_EVENT_CLASS_ERROR = "Error in Class : %s : No Event Class Specified.";
    private static final String EVENT_CLASS_NOT_ANNOTATED = "Error in Class : %s : Event Class Not annotated with @Event";
    private static final String NO_METHOD_DEFINED_ERROR = "Error in Class : %s : 'handle' method for Specified Event type  %s not defined";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ElementFilter.typesIn(roundEnv.getRootElements())
                .stream()
                .filter(this::hasHandlerAnnotation)
                .forEach(typeElement -> {
                    checkForEventClass(typeElement);
                    checkForHandlerMethod(typeElement);
                });
        return false;
    }

    /**
     * Checks if the target event class was specified and that it is annotated
     * with @Evebt
     *
     * @param typeElement the element to check
     */
    private void checkForEventClass(TypeElement typeElement) {
        if (getEventClass(typeElement.getAnnotation(Handle.class)) == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(NO_EVENT_CLASS_ERROR, typeElement));
        }
        if (getEventClass(typeElement.getAnnotation(Handle.class)) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(EVENT_CLASS_NOT_ANNOTATED, typeElement));
        }
    }

    /**
     * Gets the Class of the event to handle
     *
     * @param handles the handle annotation
     * @return a TypeMirror of the event class if found, null otherwise
     */
    private TypeMirror getEventClass(Handle handles) {
        try {
            handles.value();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }

    /**
     * Checks if a method of name handle with a single paramter for the event
     * handler class
     *
     * @param typeElement the element to check
     */
    private void checkForHandlerMethod(TypeElement typeElement) {
        List<ExecutableElement> methods
                = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        if (!methods.stream().anyMatch(m -> StringUtils.equals("handle", m.getSimpleName())
                && m.getParameters().size() == 1
                && StringUtils.equals(getEventClass(typeElement.getAnnotation(Handle.class
                )).toString(), m.getParameters().get(0).asType().toString()))) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(NO_METHOD_DEFINED_ERROR, typeElement, getEventClass(typeElement.getAnnotation(Handle.class)).toString()));
        }
    }

    private boolean hasHandlerAnnotation(TypeElement typeElement) {
        Handle handles = typeElement.getAnnotation(Handle.class
        );
        return handles != null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
