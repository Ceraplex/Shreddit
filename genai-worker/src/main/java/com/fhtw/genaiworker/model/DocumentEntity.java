package com.fhtw.genaiworker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_entity", schema = "public")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary")
    private String summary;

    @Column(name = "summary_status")
    private String summaryStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSummaryStatus() { return summaryStatus; }
    public void setSummaryStatus(String summaryStatus) { this.summaryStatus = summaryStatus; }
}
