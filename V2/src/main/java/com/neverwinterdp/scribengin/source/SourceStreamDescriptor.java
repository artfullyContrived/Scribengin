package com.neverwinterdp.scribengin.source;

public class SourceStreamDescriptor {
  protected int id;
  private String location;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SourceStreamDescriptor other = (SourceStreamDescriptor) obj;
    if (id != other.id)
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    return true;
  }
}
