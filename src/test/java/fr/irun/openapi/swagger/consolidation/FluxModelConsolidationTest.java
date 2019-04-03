package fr.irun.openapi.swagger.consolidation;

import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class FluxModelConsolidationTest {

    private FluxModelConsolidation tested;

    @BeforeEach
    void setUp() {
        tested = new FluxModelConsolidation();
    }

    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.FLUX);
    }

    @Test
    void consolidateProperty() {
        Property property = new RefProperty();

        Property outProperty = tested.consolidateProperty(property);
        assertThat(outProperty).isNotNull();
        assertThat(outProperty).isInstanceOf(ArrayProperty.class);

        ArrayProperty arrayProperty = (ArrayProperty) outProperty;
        assertThat(arrayProperty.getItems()).isSameAs(property);
    }

    @Test
    void consolidatePropertyNullProperty() {
        Property outProperty = tested.consolidateProperty(null);
        assertThat(outProperty).isNull();
    }

    @Test
    void consolidateModel() {
        Model model = new RefModel();

        Model outModel = tested.consolidateModel(model);
        assertThat(outModel).isNotNull();
        assertThat(outModel).isSameAs(model);
    }

    @Test
    void consolidateModelNullModel() {
        Model outputModel = tested.consolidateModel(null);
        assertThat(outputModel).isNull();
    }

}
