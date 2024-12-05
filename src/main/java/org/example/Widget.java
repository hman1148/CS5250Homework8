
package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Widget {

    @JsonProperty("type")
    private String type;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("widgetId")
    private String widgetId;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("label")
    private String label;

    @JsonProperty("description")
    private String description;

    @JsonProperty("otherAttributes")
    private List<SubAttributes> otherAttributes;

    public Widget() {
    }

    public Widget(String type, String requestId, String widgetId, String owner,
                  String label, String description, List<SubAttributes> otherAttributes) {
        this.type = type;
        this.requestId = requestId;
        this.widgetId = widgetId;
        this.owner = owner;
        this.label = label;
        this.description = description;
        this.otherAttributes = otherAttributes;
    }

    public Widget(String type, String requestId, String widgetId, String owner,
                  String label, String description, String field, String extraField) {
        this.type = type;
        this.requestId = requestId;
        this.widgetId = widgetId;
        this.owner = owner;
        this.label = label;
        this.description = description;
        this.otherAttributes = null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SubAttributes> getOtherAttributes() {
        return otherAttributes;
    }

    public void setOtherAttributes(List<SubAttributes> otherAttributes) {
        this.otherAttributes = otherAttributes;
    }

    public static class SubAttributes {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;

        public SubAttributes() {
        }

        public SubAttributes(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
