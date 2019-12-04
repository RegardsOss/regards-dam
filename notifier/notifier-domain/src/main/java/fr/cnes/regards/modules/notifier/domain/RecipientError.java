/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.notifier.domain;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;

/**
 * Entity to represent an error on a {@link Recipient} for in a {@link JobInfo}
 * @author Kevin Marchois
 *
 */
@Entity
@Table(name = "t_recipient_error")
public class RecipientError {

    @Id
    @SequenceGenerator(name = "recipientErrorSequence", initialValue = 1, sequenceName = "seq_recipient_error")
    @GeneratedValue(generator = "recipientErrorSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @NotNull(message = "Recipient is required")
    @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipient_id"))
    private Recipient recipient;

    @ManyToOne
    @NotNull(message = "Job info is required")
    @JoinColumn(name = "job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_id"))
    private JobInfo job;

    public Long getId() {
        return id;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public JobInfo getJob() {
        return job;
    }

    public void setJob(JobInfo job) {
        this.job = job;
    }

    public static RecipientError build(Recipient recipient, JobInfo jobInfo) {
        RecipientError error = new RecipientError();
        error.setJob(jobInfo);
        error.setRecipient(recipient);

        return error;
    }

}
