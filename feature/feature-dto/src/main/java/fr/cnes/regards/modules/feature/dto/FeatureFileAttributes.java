/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.feature.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.MimeType;

import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.utils.file.validation.HandledMessageDigestAlgorithm;

/**
 * File attributes
 *
 * @author Marc SORDI
 */
public class FeatureFileAttributes {

	@NotNull(message = "Data type is required")
	private DataType dataType;

	/**
	 * Required {@link MimeType}
	 */
	@NotNull(message = "MIME type is required")
	private MimeType mimeType;

	/**
	 * The file name
	 */
	@NotBlank(message = "Filename is required")
	private String filename;

	/**
	 * The file size
	 */
	private Long filesize;

	/**
	 * The checksum algorithm (<b>required</b> if data object is not a reference)
	 */
	@HandledMessageDigestAlgorithm
	private String algorithm;

	/**
	 * The checksum (<b>required</b> if data object is not a reference)
	 */
	private String checksum;

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public MimeType getMimeType() {
		return mimeType;
	}

	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long fileSize) {
		this.filesize = fileSize;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (algorithm == null ? 0 : algorithm.hashCode());
		result = (prime * result) + (checksum == null ? 0 : checksum.hashCode());
		result = (prime * result) + (dataType == null ? 0 : dataType.hashCode());
		result = (prime * result) + (filesize == null ? 0 : filesize.hashCode());
		result = (prime * result) + (filename == null ? 0 : filename.hashCode());
		result = (prime * result) + (mimeType == null ? 0 : mimeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FeatureFileAttributes other = (FeatureFileAttributes) obj;
		if (algorithm == null) {
			if (other.algorithm != null) {
				return false;
			}
		} else if (!algorithm.equals(other.algorithm)) {
			return false;
		}
		if (checksum == null) {
			if (other.checksum != null) {
				return false;
			}
		} else if (!checksum.equals(other.checksum)) {
			return false;
		}
		if (dataType != other.dataType) {
			return false;
		}
		if (filesize == null) {
			if (other.filesize != null) {
				return false;
			}
		} else if (!filesize.equals(other.filesize)) {
			return false;
		}
		if (filename == null) {
			if (other.filename != null) {
				return false;
			}
		} else if (!filename.equals(other.filename)) {
			return false;
		}
		if (mimeType == null) {
			if (other.mimeType != null) {
				return false;
			}
		} else if (!mimeType.equals(other.mimeType)) {
			return false;
		}
		return true;
	}

	public static FeatureFileAttributes build(DataType dataType, MimeType mimeType, String filename, Long fileSize,
			String algorithm, String checksum) {
		FeatureFileAttributes attribute = new FeatureFileAttributes();
		attribute.setDataType(dataType);
		attribute.setMimeType(mimeType);
		attribute.setFilename(filename);
		attribute.setFilesize(fileSize);
		attribute.setAlgorithm(algorithm);
		attribute.setChecksum(checksum);

		return attribute;
	}

}
