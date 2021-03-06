/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct.ap.test.reverse;

import javax.tools.Diagnostic.Kind;

import org.mapstruct.ap.test.reverse.erroneous.SourceTargetMapperAmbiguous1;
import org.mapstruct.ap.test.reverse.erroneous.SourceTargetMapperAmbiguous2;
import org.mapstruct.ap.test.reverse.erroneous.SourceTargetMapperAmbiguous3;
import org.mapstruct.ap.test.reverse.erroneous.SourceTargetMapperNonMatchingName;
import org.mapstruct.ap.testutil.IssueKey;
import org.mapstruct.ap.testutil.ProcessorTest;
import org.mapstruct.ap.testutil.WithClasses;
import org.mapstruct.ap.testutil.compilation.annotation.CompilationResult;
import org.mapstruct.ap.testutil.compilation.annotation.Diagnostic;
import org.mapstruct.ap.testutil.compilation.annotation.ExpectedCompilationOutcome;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sjaak Derksen
 */
@IssueKey("252")
@WithClasses({ Source.class, Target.class })
public class InheritInverseConfigurationTest {

    @ProcessorTest
    @WithClasses({ SourceTargetMapper.class })
    public void shouldInheritInverseConfigurationMultipleCandidates() {

        Source source = new Source();
        source.setPropertyToIgnoreDownstream( "propToIgnoreDownStream" );
        source.setStringPropX( "1" );
        source.setIntegerPropX( 2 );

        Target target = SourceTargetMapper.INSTANCE.forward( source );
        assertThat( target ).isNotNull();
        assertThat( target.getStringPropY() ).isEqualTo( "1" );
        assertThat( target.getIntegerPropY() ).isEqualTo( 2 );
        assertThat( target.getPropertyNotToIgnoreUpstream() ).isEqualTo( "propToIgnoreDownStream" );

        source = SourceTargetMapper.INSTANCE.reverse( target );
        assertThat( source ).isNotNull();
        assertThat( source.getStringPropX() ).isEqualTo( "1" );
        assertThat( source.getIntegerPropX() ).isEqualTo( 2 );
        assertThat( source.getSomeConstantDownstream() ).isEqualTo( "test" );
        assertThat( source.getPropertyToIgnoreDownstream() ).isNull();
    }

    @ProcessorTest
    @WithClasses({ SourceTargetMapperAmbiguous1.class })
    @ExpectedCompilationOutcome(
        value = CompilationResult.FAILED,
        diagnostics = {
            @Diagnostic(type = SourceTargetMapperAmbiguous1.class,
                kind = Kind.ERROR,
                line = 38,
                message = "Several matching inverse methods exist: forward(), "
                    + "forwardNotToReverse(). Specify a name explicitly."),
            @Diagnostic(type = SourceTargetMapperAmbiguous1.class,
                kind = Kind.WARNING,
                line = 43,
                message = "Unmapped target properties: \"stringPropX, integerPropX\".")
        }
    )
    public void shouldRaiseAmbiguousReverseMethodError() {
    }

    @ProcessorTest
    @WithClasses({ SourceTargetMapperAmbiguous2.class })
    @ExpectedCompilationOutcome(
        value = CompilationResult.FAILED,
        diagnostics = {
            @Diagnostic(type = SourceTargetMapperAmbiguous2.class,
                kind = Kind.ERROR,
                line = 38,
                message = "None of the candidates forward(), forwardNotToReverse() matches given "
                    + "name: \"blah\"."),
            @Diagnostic(type = SourceTargetMapperAmbiguous2.class,
                kind = Kind.WARNING,
                line = 43,
                message = "Unmapped target properties: \"stringPropX, integerPropX\".")
        }
    )
    public void shouldRaiseAmbiguousReverseMethodErrorWrongName() {
    }

    @ProcessorTest
    @WithClasses({ SourceTargetMapperAmbiguous3.class })
    @ExpectedCompilationOutcome(
        value = CompilationResult.FAILED,
        diagnostics = {
            @Diagnostic(type = SourceTargetMapperAmbiguous3.class,
                kind = Kind.ERROR,
                line = 39,
                message = "Given name \"forward\" matches several candidate methods: org.mapstruct.ap.test.reverse" +
                    ".Target forward(org.mapstruct.ap.test.reverse.Source source), org.mapstruct.ap.test.reverse" +
                    ".Target forward(org.mapstruct.ap.test.reverse.Source source, @MappingTarget org.mapstruct.ap" +
                    ".test.reverse.Target target)."),
            @Diagnostic(type = SourceTargetMapperAmbiguous3.class,
                kind = Kind.WARNING,
                line = 44,
                message = "Unmapped target properties: \"stringPropX, integerPropX\".")
        }
    )
    public void shouldRaiseAmbiguousReverseMethodErrorDuplicatedName() {
    }

    @ProcessorTest
    @WithClasses({ SourceTargetMapperNonMatchingName.class })
    @ExpectedCompilationOutcome(
        value = CompilationResult.FAILED,
        diagnostics = {
            @Diagnostic(type = SourceTargetMapperNonMatchingName.class,
                kind = Kind.ERROR,
                line = 31,
                message = "Given name \"blah\" does not match the only candidate. Did you mean: \"forward\"."),
            @Diagnostic(type = SourceTargetMapperNonMatchingName.class,
                kind = Kind.WARNING,
                line = 36,
                message = "Unmapped target properties: \"stringPropX, integerPropX\".")
        }
    )
    public void shouldAdviseOnSpecifyingCorrectName() {
    }

}
