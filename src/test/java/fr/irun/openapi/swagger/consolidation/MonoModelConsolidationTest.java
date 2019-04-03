package fr.irun.openapi.swagger.consolidation;

import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class MonoModelConsolidationTest {

    private MonoModelConsolidation tested;

    private Property property;
    private Model model;

    @BeforeEach
    void setUp() {
        property = mock(Property.class);
        model = mock(Model.class);

        tested = new MonoModelConsolidation();
    }

    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.MONO);
    }

    @Test
    void consolidateProperty() {
        Property outProperty = tested.consolidateProperty(property);
        assertThat(outProperty).isNotNull();
        assertThat(outProperty).isSameAs(property);
        // Check no modification has been done on the instance
        verifyNoMoreInteractions(property);
    }

    @Test
    void consolidatePropertyNullProperty() {
        Property outProperty = tested.consolidateProperty(null);
        assertThat(outProperty).isNull();
    }


    @Test
    void consolidateModel() {
        Model outModel = tested.consolidateModel(model);
        assertThat(outModel).isNotNull();
        assertThat(outModel).isSameAs(model);
        // Check no modification has been done on the instance
        verifyNoMoreInteractions(model);
    }

    @Test
    void consolidateModelNullModel() {
        Model outputModel = tested.consolidateModel(null);
        assertThat(outputModel).isNull();
    }
}