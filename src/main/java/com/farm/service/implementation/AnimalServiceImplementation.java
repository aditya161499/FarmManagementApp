package com.farm.service.implementation;

import com.farm.dao.AnimalEntity;
import com.farm.exceptions.ApplicationException;
import com.farm.model.Animal;
import com.farm.model.Weight;
import com.farm.repository.AnimalRepository;
import com.farm.service.IAnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.farm.mappers.EntityModelMappers.*;

@Service
public class AnimalServiceImplementation implements IAnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private WeightServiceImplementation weightServiceImplementation;

    @Override
    public List<Animal> findAll() {
        List<AnimalEntity> animalEntityList = (List<AnimalEntity>) animalRepository.findAll();
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByTypeId(int typeId) {

        List<AnimalEntity> animalEntityList = animalRepository.findByAnimalType(typeId);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByType(int animalTypeId) {

        List<AnimalEntity> animalEntityList = animalRepository.findByAnimalType(animalTypeId);

        return handleList(animalEntityList);
    }

    @Override
    public List<Animal> findByTypeDeadLessThanSixMonths(int animalTypeId) {

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        System.out.println(sixMonthsAgo);
        List<AnimalEntity> animalEntityList1 = animalRepository.findByAnimalTypeAndDateDeathIsAfter(animalTypeId, java.sql.Date.valueOf(sixMonthsAgo));
        List<AnimalEntity> animalEntityList2 = animalRepository.findByAnimalTypeAndDateDeathIsNull(animalTypeId);

        List<AnimalEntity> animalEntityList = new ArrayList<>();
        animalEntityList.addAll(animalEntityList1);
        animalEntityList.addAll(animalEntityList2);

        return handleList(animalEntityList);
    }

    @Override
    public List<Animal> findByTypeAlive(int animalTypeId) {

        List<AnimalEntity> animalEntityList = animalRepository.findByAnimalTypeAndDateDeathIsNullAndDateDepartureIsNull(animalTypeId);
        return handleList(animalEntityList);
    }

    @Override
    public List<Animal> findByAge(int age) {

        int currentYear = LocalDate.now().getYear();
        int yearSearched = currentYear - age;

        List<AnimalEntity> animalEntityList = animalRepository.findByDateBirthContaining(yearSearched);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findBySex(String sex) {
        List<AnimalEntity> animalEntityList =  animalRepository.findByAnimalSex(sex);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByArrivalDate(Date date) {
        List<AnimalEntity> animalEntityList = animalRepository.findByDateArrival(date);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByDepartureDate(Date date) {
        List<AnimalEntity> animalEntityList =  animalRepository.findByDateDeparture(date);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByMotherId(int id) {
        List<AnimalEntity> animalEntityList =  animalRepository.findByMotherId(id);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public List<Animal> findByFatherId(int id) {
        List<AnimalEntity> animalEntityList =  animalRepository.findByFatherId(id);
        return parseAnimalList(animalEntityList);
    }

    @Override
    public Animal findById(int id) {
        AnimalEntity animalEntity = animalRepository.findByAnimalId(id);

        Animal animal = parseAnimalEntity(animalEntity);
        List<Weight> weights = weightServiceImplementation.findByAnimalId(id);
        animal.setWeights(weights);
        return animal;
    }

    @Override
    public Animal findByName(String name) {
        AnimalEntity animalEntity = animalRepository.findByAnimalName(name);
        return parseAnimalEntity(animalEntity);
    }

    @Override
    public Animal save(Animal animal) throws ApplicationException {

        Animal animalSaved;

        if(checkDatesAnimalOK(animal) && checkDeathCause(animal)) {

            if(checkStateValid(animal)) {
                AnimalEntity animalEntity = parseAnimal(animal);
                AnimalEntity animalEntitySaved = animalRepository.save(animalEntity);
                animalSaved = parseAnimalEntity(animalEntitySaved);
            }
            else {
                throw new ApplicationException("Error: The state is invalid");
            }
        }
        else {
            throw new ApplicationException("Error: Some of the dates are invalid");
        }

        return animalSaved;
    }

    @Override
    public Animal update(int id, Animal animal) throws ApplicationException  {

        AnimalEntity animalEntity = animalRepository.findByAnimalId(id);
        Animal animalUpdated;

        if(animalEntity != null) {

           animalUpdated = save(animal);
        }
        else {
            throw new ApplicationException("Error: Animal does not exist");
        }

        return animalUpdated;

    }

    @Override
    public boolean deleteById(int id) throws ApplicationException {


        AnimalEntity animalEntity = animalRepository.findByAnimalId(id);
        boolean deleted;

        if(animalEntity != null) {

            if(checkIfHasChildren(animalEntity)) {

                if(weightServiceImplementation.deleteByAnimalId(id)) {
                   animalRepository.deleteById(id);
                   deleted = true;
               }
               else {
                    throw new ApplicationException("Error: Could not delete associated weights");
               }

            } else {
                throw new ApplicationException("Error: This animal has descendants and cannot be deleted");
            }

        } else {
            throw new ApplicationException("Error: Animal does not exist");
        }

        return deleted;
    }

    @Override
    public void deleteByName(String name) {
        animalRepository.deleteByAnimalName(name);
    }

    private boolean checkDatesAnimalOK(Animal animal) {

        LocalDate today = LocalDate.now();
        LocalDate birthDate = animal.getBirth() == null ? today.minusDays(1) : animal.getBirth();
        LocalDate deathDate = animal.getDeath() == null ? today : animal.getDeath();
        LocalDate arrivalDate = animal.getArrival() == null ? birthDate : animal.getArrival();
        LocalDate departureDate = animal.getDeparture() == null ? today : animal.getDeparture();

        return ((deathDate.isAfter(birthDate) || deathDate.isEqual(birthDate))
                && (departureDate.isAfter(arrivalDate) || departureDate.isEqual(arrivalDate))
                && (arrivalDate.isAfter(birthDate) || arrivalDate.isEqual(birthDate))
                && (deathDate.isBefore(today) || deathDate.isEqual(today))
                && (birthDate.isBefore(today) || birthDate.isEqual(today))
        );

    }

    private boolean checkDeathCause(Animal animal) {
        
        return (animal.getDeathCause() == null || (animal.getDeathCause() != null && animal.getDeath() != null));
    }

    private boolean checkStateValid(Animal animal) {

        LocalDate today = LocalDate.now();
        LocalDate sixMonthAgo = LocalDate.now().minusMonths(6);
        LocalDate birthDate = animal.getBirth() == null ? today.minusDays(1) : animal.getBirth();
        LocalDate deathDate = animal.getDeath();
        LocalDate departureDate = animal.getDeparture();

        String state = animal.getState();

        return ((state.equals("teen") && birthDate.isAfter(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("pregnant") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("nursing") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("resting") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("retired") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("supermale") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("fattening") && birthDate.isBefore(sixMonthAgo) && deathDate == null && departureDate == null)
                || (state.equals("dead") && deathDate != null)
                || (state.equals("sold") && departureDate != null));

    }

    private boolean checkIfHasChildren(AnimalEntity animalEntity) {

        List<AnimalEntity> animalEntityListChild;

        if(animalEntity.getAnimalSex().equals("F")) {
            animalEntityListChild = animalRepository.findByMotherId(animalEntity.getAnimalId());
        }
        else {
            animalEntityListChild = animalRepository.findByFatherId(animalEntity.getAnimalId());
        }

        return (animalEntityListChild.isEmpty());

    }

    private List<Animal> handleList(List<AnimalEntity> animalEntityList) {

        List<Animal> animalList = parseAnimalList(animalEntityList);

        for(Animal animal : animalList) {

            List<Weight> weights = weightServiceImplementation.findByAnimalId(animal.getId());
            animal.setWeights(weights);
        }

        return animalList;
    }
}
