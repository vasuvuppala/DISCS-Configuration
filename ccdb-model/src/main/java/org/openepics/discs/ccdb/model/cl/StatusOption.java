/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 *
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 *
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 *
 */

package org.openepics.discs.ccdb.model.cl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openepics.discs.ccdb.model.ConfigurationEntity;

/**
 * Status values that a field in a checklist can take
 * 
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cl_status_option",
       uniqueConstraints=@UniqueConstraint(columnNames={"checklist", "name"}))
@NamedQueries({
    @NamedQuery(name = "StatusOption.findAll", query = "SELECT d FROM StatusOption d"),
    @NamedQuery(name = "StatusOption.findByChecklist", query = "SELECT d FROM StatusOption d WHERE d.checklist = :checklist"),    
    @NamedQuery(name = "StatusOption.findByName", query = "SELECT d FROM StatusOption d WHERE  d.name = :name")
})
public class StatusOption extends ConfigurationEntity {

    private static final long serialVersionUID = 1L; 
    
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "checklist")
    private Checklist checklist;
    
    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name = "name")
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=255)
    @Column(name = "description")
    private String description;
 
    @Basic(optional = false)
    @NotNull
    @Column(name = "logical_value")
    private Boolean logicalValue = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "completed")
    private Boolean completed = false; // if true for a summary phase, rest of the phases in the group are frozen.
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "weight")
    private Integer weight = 0; // relative importance of the options. summary phase must be greater than or equal to the rest of the phases.
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "comment_req")
    private Boolean commentRequired = false; // if true for an explanation is required
    
    
    // getters and setters

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

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public Boolean getLogicalValue() {
        return logicalValue;
    }

    public void setLogicalValue(Boolean logicalValue) {
        this.logicalValue = logicalValue;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Boolean getCommentRequired() {
        return commentRequired;
    }

    public void setCommentRequired(Boolean commentRequired) {
        this.commentRequired = commentRequired;
    }
    
}