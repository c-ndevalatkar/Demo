package com.symphony.applaunch.repository;

import com.symphony.applaunch.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserDAO extends IGenericDAO<Users, Long>{

    /**
     * method is used to find whether userid/email id is already registered
     * @param emailId
     * @return {@link Users}
     */
    public Users findByEmail(String emailId);

    /**
     * method is used to find user by id
     * @param id
     * @return {@link Users}
     */
    public Users findOne(Long id);

    /**
     * method is used to save user info to database
     * @param user
     * @return
     */
    public Users saveUserData(Users user);
    /**
     * method is used to update user info to database
     * @param user
     * @return
     */
    public Users updateUserData(Users user);
    /**
     * this method used to find verified users from database.
     * @param pageable
     * @return verified user list
     */
    public Page<Users> findAllTokenVerifiedUsers(Pageable pageable);


    /**
     * this method used to delete recoed by userId
     * @param userId
     * @return
     */

    public Long deleteByUserId(Long userId);

    /**
     * method is used to find user info by adName from database
     * @param adName
     * @return {@link Users}
     */
    public Users findByAdName(String adName);



    /**
     * method is used to find user info by according to  role from database
     * @param role
     * @return {@link Users}
     */
    public List<Users> findUsersByRole(UserRoles role);

    /**
     * method is used to verify token for particular user.
     * @param token
     * @return {@link Users}
     */
    public Users verifyToken(String token);

    /**
     * method is used to find global user for particular company.
     * @param companyId,userToken
     * @return list {@link Users}
     */
    public List<Users> getGlobalUsersByCompany(Long companyId, String userToken);

    
	public List<String> getDimentionByUserId(long userId);

	public List<DimensionDTO> findMdmDimensionsByUserId(String userId);

}
