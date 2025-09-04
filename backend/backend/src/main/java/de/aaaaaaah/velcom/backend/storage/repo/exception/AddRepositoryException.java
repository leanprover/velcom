package de.aaaaaaah.velcom.backend.storage.repo.exception;

import java.util.Objects;

/** An exception that occurs when trying to add a repository to a repo storage fails. */
public class AddRepositoryException extends Exception {

  private final String dirName;
  private final String remoteUrl;

  /**
   * Constructs a new {@link AddRepositoryException}.
   *
   * @param dirName the directory name for the repository
   * @param remoteUrl the remote url of the repository
   * @param cause the cause
   */
  public AddRepositoryException(String dirName, String remoteUrl, Throwable cause) {
    super("failed to add repository to directory \"" + dirName + "\": " + remoteUrl, cause);
    this.dirName = Objects.requireNonNull(dirName);
    this.remoteUrl = Objects.requireNonNull(remoteUrl);
  }

  /**
   * @return Returns the directory that should have been the directory for the repository
   */
  public String getDirName() {
    return dirName;
  }

  /**
   * @return Returns the remote url of the repository
   */
  public String getRemoteUrl() {
    return remoteUrl;
  }
}
