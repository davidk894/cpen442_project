package cpen442.securefileshare;

public class Job {
    private int jobType; // Got key, pending response, or pending request
    private String fileHash;
    private String jobID;

    private String userID;

    private String contactNumber;
    private String name;

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) { jobType = jobType; }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        fileHash = fileHash;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        userID = userID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        jobID = jobID;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
