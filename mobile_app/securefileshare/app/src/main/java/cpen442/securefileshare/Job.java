package cpen442.securefileshare;

public class Job {
    private int jobType; // Got key, pending response, or pending request
    private String fileHash;
    private String jobId;

    private String userID;

    private String contactNumber;
    private String name;

    public int getJobType() {
        return this.jobType;
    }

    public void setJobType(int jobType) { this.jobType = jobType; }

    public String getFileHash() {
        return this.fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getJobID() {
        return this.jobId;
    }

    public void setJobID(String jobID) {
        this.jobId = jobID;
    }

    public String getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
