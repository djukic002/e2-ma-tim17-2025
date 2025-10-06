package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.data.repositories.UserItemRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.User;
import com.example.valorquest.model.UserItem;
import com.example.valorquest.model.dto.UserItemWithEquipmentDto;
import com.example.valorquest.model.enums.MissionContributionType;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.BossService;
import com.example.valorquest.service.EquipmentService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossFightViewmodel extends ViewModel {
    private final BossService bossService;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    private final AllianceMissionService missionService;
    @Inject
    public BossFightViewmodel(BossService bossService, AllianceMissionService missionService) {
        this.bossService = bossService;
        this.userRepository = new UserRepository();
        this.equipmentRepository = new EquipmentRepository();
        this.missionService = missionService;
    }

    public void contributeToMission(MissionContributionType type, RepositoryCallback<Boolean> callback) {
        missionService.contribute(type, callback);
    }
    public LiveData<Result<String>> saveBossLiveData(Boss boss) {
        return bossService.saveBoss(boss);
    }
    public LiveData<Result<Boss>> getActiveBossForUser(String userId) {
        return bossService.getActiveBossForUserLiveData(userId);
    }
    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        userRepository.getById(userId, user -> {
            userLiveData.postValue(user);
        });
        return userLiveData;
    }
    public LiveData<List<UserItem>> getUserItems(String userId) {
        UserItemRepository userItemRepository = new UserItemRepository(userId);
        return userItemRepository.getUserItems();
    }

    public void damageEquipment() {
        EquipmentService equipmentService = new EquipmentService(equipmentRepository, userRepository, new BossRepository());
        equipmentService.damageEquipment();
    }

    public void getUserItemsWithEquipment(String userId, RepositoryCallback<List<UserItemWithEquipmentDto>> callback) {
        UserItemRepository userItemRepo = new UserItemRepository(userId);
        EquipmentRepository equipmentRepo = new EquipmentRepository();

        userItemRepo.getAllItems(userItems -> {
            if (userItems == null) {
                callback.onComplete(null);
                return;
            }

            List<UserItemWithEquipmentDto> dtoList = new ArrayList<>();
            for (UserItem item : userItems) {
                if (!item.isActivated())
                    continue;
                equipmentRepo.getById(item.getEquipmentId(), equipment -> {
                    if (equipment != null) {
                        dtoList.add(new UserItemWithEquipmentDto(item, equipment));
                    }

                    if (dtoList.size() == userItems.size()) {
                        callback.onComplete(dtoList);
                    }
                });
            }
        });
    }
    public LiveData<List<UserItemWithEquipmentDto>> getUserItemsWithEquipmentLiveData(String userId) {
        MutableLiveData<List<UserItemWithEquipmentDto>> liveData = new MutableLiveData<>();

        // Call your callback-based method
        getUserItemsWithEquipment(userId, dtoList -> liveData.postValue(dtoList));

        return liveData;
    }
}
