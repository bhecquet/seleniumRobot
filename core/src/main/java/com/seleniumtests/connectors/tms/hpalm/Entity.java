package com.seleniumtests.connectors.tms.hpalm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fields" })
@XmlRootElement(name = "Entity")
public class Entity {
	/**
	 *
	 * Java class for anonymous complex type.
	 *
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 *
	 * 
	 * <complexType> <complexContent> <restriction
	 * base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence> <element
	 * name="Fields"> <complexType> <complexContent> <restriction
	 * base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence> <element
	 * name="Field" maxOccurs="unbounded"> <complexType> <complexContent>
	 * <restriction base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence>
	 * <element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"
	 * maxOccurs="unbounded"/> </sequence> <attribute name="Name" use="required"
	 * type="{http://www.w3.org/2001/XMLSchema}string" /> </restriction>
	 * </complexContent> </complexType> </element> </sequence> </restriction>
	 * </complexContent> </complexType> </element> </sequence> <attribute
	 * name="Type" use="required"
	 * type="{http://www.w3.org/2001/XMLSchema}string" /> </restriction>
	 * </complexContent> </complexType>
	 * 
	 * 
	 *
	 *
	 */
	@XmlElement(name = "Fields", required = true)
	protected Entity.Fields fields;
	@XmlAttribute(name = "Type", required = true)
	protected String type;

	/**
	 * @param entity
	 */
	public Entity(Entity entity) {
		type = new String(entity.getType());
		fields = new Entity.Fields(entity.getFields());
	}

	/**
	 *
	 */
	public Entity() {
		// nothing
	}

	/**
	 * Gets the value of the fields property.
	 *
	 * @return possible object is {@link Entity.Fields }
	 *
	 */
	public Entity.Fields getFields() {
		return fields;
	}

	/**
	 * Sets the value of the fields property.
	 *
	 * @param value
	 *            allowed object is {@link Entity.Fields }
	 *
	 */
	public void setFields(Entity.Fields value) {
		this.fields = value;
	}

	/**
	 * Gets the value of the type property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setType(String value) {
		this.type = value;
	}

	/**
	 * Java class for anonymous complex type.
	 *
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 *
	 * 
	 * <complexType> <complexContent> <restriction
	 * base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence> <element
	 * name="Field" maxOccurs="unbounded"> <complexType> <complexContent>
	 * <restriction base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence>
	 * <element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"
	 * maxOccurs="unbounded"/> </sequence> <attribute name="Name" use="required"
	 * type="{http://www.w3.org/2001/XMLSchema}string" /> </restriction>
	 * </complexContent> </complexType> </element> </sequence> </restriction>
	 * </complexContent> </complexType>
	 * 
	 * 
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "field" })
	public static class Fields {
		@XmlElement(name = "Field", required = true)
		protected List<Entity.Fields.Field> field;

		/**
		 * @param fields
		 */
		public Fields(Fields fields) {
			field = new ArrayList<>(fields.getField());
		}

		/**
	 *
	 */
		public Fields() {
			// nothing
		}

		/**
		 * Gets the value of the field property.
		 *
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * set method for the field property.
		 *
		 *
		 * For example, to add a new item, do as follows:
		 *
		 * 
		 * getField().add(newItem);
		 * 
		 * 
		 *
		 *
		 * Objects of the following type(s) are allowed in the list
		 * {@link Entity.Fields.Field }
		 *
		 *
		 */
		public List<Entity.Fields.Field> getField() {
			if (field == null) {
				field = new ArrayList<>();
			}
			return this.field;
		}

		/**
		 * @param value
		 *            Field to be added to the fields.
		 */
		public void addField(Entity.Fields.Field value) {
			this.getField().add(value);
		}

		/**
		 * Java class for anonymous complex type.
		 *
		 * The following schema fragment specifies the expected content
		 * contained within this class.
		 *
		 * 
		 * <complexType> <complexContent> <restriction
		 * base="{http://www.w3.org/2001/XMLSchema}anyType"> <sequence> <element
		 * name="Value" type="{http://www.w3.org/2001/XMLSchema}string"
		 * maxOccurs="unbounded"/> </sequence> <attribute name="Name"
		 * use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
		 * </restriction> </complexContent> </complexType>
		 * 
		 * 
		 *
		 *
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "value" })
		public static class Field {
			@XmlElement(name = "Value", required = true)
			protected List<String> value;
			@XmlAttribute(name = "Name", required = true)
			protected String name;

			/**
			 * Gets the value of the value property.
			 *
			 * This accessor method returns a reference to the live list, not a
			 * snapshot. Therefore any modification you make to the returned
			 * list will be present inside the JAXB object. This is why there is
			 * not a set method for the value property.
			 *
			 * For example, to add a new item, do as follows:
			 *
			 * 
			 * getValue().add(newItem);
			 * 
			 * 
			 *
			 *
			 * Objects of the following type(s) are allowed in the list
			 * {@link String }
			 *
			 *
			 */
			public List<String> getValue() {
				if (value == null) {
					value = new ArrayList<>();
				}
				return this.value;
			}

			/**
			 * Gets the value of the name property.
			 *
			 * @return possible object is {@link String }
			 *
			 */
			public String getName() {
				return name;
			}

			/**
			 * Sets the value of the name property.
			 *
			 * @param value
			 *            allowed object is {@link String }
			 *
			 */
			public void setName(String value) {
				this.name = value;
			}

			/**
			 * Sets the value of the value property.
			 *
			 * @param value
			 *            allowed object is {@link String }
			 *
			 */
			public void addValue(String value) {
				this.getValue().add(value);
			}
		}
	}
}
