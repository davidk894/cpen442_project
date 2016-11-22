package cpen442.securefileshare;

public class Job {
    private int JobType; // Got key or pending response
    private String FileHash;
    private String UserID;
    private String JobID;

    public int getJobType() {
        return JobType;
    }

    public void setJobType(int jobType) {
        JobType = jobType;
    }

    public String getFileHash() {
        return FileHash;
    }

    public void setFileHash(String fileHash) {
        FileHash = fileHash;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getJobID() {
        return JobID;
    }

    public void setJobID(String jobID) {
        JobID = jobID;
    }

}
