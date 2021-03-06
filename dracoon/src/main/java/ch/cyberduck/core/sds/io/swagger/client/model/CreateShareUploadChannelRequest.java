/*
 * DRACOON
 * REST Web Services for DRACOON<br>Version: 4.20.1  - built at: 2020-04-05 23:00:17<br><br><a title='Developer Information' href='https://developer.dracoon.com'>Developer Information</a>&emsp;&emsp;<a title='Get SDKs on GitHub' href='https://github.com/dracoon'>Get SDKs on GitHub</a>
 *
 * OpenAPI spec version: 4.20.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.sds.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Request model for creating an upload channel
 */
@ApiModel(description = "Request model for creating an upload channel")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class CreateShareUploadChannelRequest {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("size")
  private Long size = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("directS3Upload")
  private Boolean directS3Upload = null;

  public CreateShareUploadChannelRequest name(String name) {
    this.name = name;
    return this;
  }

   /**
   * File name
   * @return name
  **/
  @ApiModelProperty(required = true, value = "File name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CreateShareUploadChannelRequest size(Long size) {
    this.size = size;
    return this;
  }

   /**
   * File size in byte
   * @return size
  **/
  @ApiModelProperty(value = "File size in byte")
  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public CreateShareUploadChannelRequest password(String password) {
    this.password = password;
    return this;
  }

   /**
   * Password
   * @return password
  **/
  @ApiModelProperty(value = "Password")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public CreateShareUploadChannelRequest directS3Upload(Boolean directS3Upload) {
    this.directS3Upload = directS3Upload;
    return this;
  }

   /**
   * Upload direct to S3 (default: &#x60;false&#x60;)
   * @return directS3Upload
  **/
  @ApiModelProperty(value = "Upload direct to S3 (default: `false`)")
  public Boolean isDirectS3Upload() {
    return directS3Upload;
  }

  public void setDirectS3Upload(Boolean directS3Upload) {
    this.directS3Upload = directS3Upload;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateShareUploadChannelRequest createShareUploadChannelRequest = (CreateShareUploadChannelRequest) o;
    return Objects.equals(this.name, createShareUploadChannelRequest.name) &&
        Objects.equals(this.size, createShareUploadChannelRequest.size) &&
        Objects.equals(this.password, createShareUploadChannelRequest.password) &&
        Objects.equals(this.directS3Upload, createShareUploadChannelRequest.directS3Upload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, size, password, directS3Upload);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateShareUploadChannelRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    directS3Upload: ").append(toIndentedString(directS3Upload)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

