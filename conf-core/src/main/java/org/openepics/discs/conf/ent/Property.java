package org.openepics.discs.conf.ent;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author vuppala
 */
@Entity
@Table(name = "property")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Property.findAll", query = "SELECT p FROM Property p"),
    @NamedQuery(name = "Property.findByPropertyId", query = "SELECT p FROM Property p WHERE p.id = :id"),
    @NamedQuery(name = "Property.findByName", query = "SELECT p FROM Property p WHERE p.name = :name"),
    @NamedQuery(name = "Property.findByDescription", query = "SELECT p FROM Property p WHERE p.description = :description"),
    @NamedQuery(name = "Property.findByAssociation", query = "SELECT p FROM Property p WHERE p.association = :association"),
    @NamedQuery(name = "Property.findByModifiedAt", query = "SELECT p FROM Property p WHERE p.modifiedAt = :modifiedAt"),
    @NamedQuery(name = "Property.findByModifiedBy", query = "SELECT p FROM Property p WHERE p.modifiedBy = :modifiedBy"),
    @NamedQuery(name = "Property.findByVersion", query = "SELECT p FROM Property p WHERE p.version = :version")})
public class Property extends ConfigurationEntity {
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "description")
    private String description;

    @Basic(optional = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "association", length = 12)
    private PropertyAssociation association;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property")
    private List<ComptypeProperty> comptypePropertyList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property")
    private List<DeviceProperty> devicePropertyList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property")
    private List<AlignmentProperty> alignmentPropertyList;

    @JoinColumn(name = "data_type", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private DataType dataType;

    @JoinColumn(name = "unit", referencedColumnName = "id")
    @ManyToOne
    private Unit unit;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property")
    private List<SlotProperty> slotPropertyList;

    protected Property() {
    }

    public Property(String name, String description, PropertyAssociation association, String modifiedBy) {
        this.name = name;
        this.description = description;
        this.association = association;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PropertyAssociation getAssociation() {
        return association;
    }

    public void setAssociation(PropertyAssociation association) {
        this.association = association;
    }

    @XmlTransient
    public List<ComptypeProperty> getComptypePropertyList() {
        return comptypePropertyList;
    }

    public void setComptypePropertyList(List<ComptypeProperty> comptypePropertyList) {
        this.comptypePropertyList = comptypePropertyList;
    }

    @XmlTransient
    public List<DeviceProperty> getDevicePropertyList() {
        return devicePropertyList;
    }

    public void setDevicePropertyList(List<DeviceProperty> devicePropertyList) {
        this.devicePropertyList = devicePropertyList;
    }

    @XmlTransient
    public List<AlignmentProperty> getAlignmentPropertyList() {
        return alignmentPropertyList;
    }

    public void setAlignmentPropertyList(List<AlignmentProperty> alignmentPropertyList) {
        this.alignmentPropertyList = alignmentPropertyList;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @XmlTransient
    public List<SlotProperty> getSlotPropertyList() {
        return slotPropertyList;
    }

    public void setSlotPropertyList(List<SlotProperty> slotPropertyList) {
        this.slotPropertyList = slotPropertyList;
    }

    @Override
    public String toString() {
        return "Property[ propertyId=" + id + " ]";
    }

}
