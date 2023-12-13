package dev.findfirst.users.service;

import dev.findfirst.security.userAuth.execeptions.NoTokenFoundException;
import dev.findfirst.security.userAuth.execeptions.NoUserFoundException;
import dev.findfirst.security.userAuth.execeptions.TokenExpiredException;
import dev.findfirst.users.model.user.Token;
import dev.findfirst.users.model.user.User;
import java.util.Calendar;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService extends AccountService {

  public RegistrationService(UserManagementService userManagement, DefaultEmailService email) {
    super(userManagement, email);
  }

  public void sendRegistration(User user) {
    var token = userManagement.createVerificationToken(user);
    AccountEmailOp(user.getEmail(), token);
  }

  @Override
  void AccountEmailOp(String emailAddres, String token) {
    String confirmationUrl = domain + "/user/regitrationConfirm?token=" + token;
    String message =
        """
            Please finish registring your account with the given url:
            %s

            Sincerly,
            Findfirst team!
        """
            .formatted(confirmationUrl);

    emailService.sendSimpleEmail(emailAddres, "Account Registration", message);
  }

  public void registrationComplete(String token)
      throws NoTokenFoundException, TokenExpiredException, NoUserFoundException {
    Token verificationToken = userManagement.getVerificationToken(token);
    if (verificationToken == null) {
      throw new NoTokenFoundException();
    }

    User user = verificationToken.getUser();
    if (user == null) {
      throw new NoUserFoundException();
    }
    Calendar cal = Calendar.getInstance();
    if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
      throw new TokenExpiredException();
    }
    user.setEnabled(true);
    userManagement.saveUser(user);
  }
}