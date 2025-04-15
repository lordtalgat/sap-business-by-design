package kz.talgat.models

case class WorkplaceAddressInformation(countryCode: String,
                                       regionCode: String,
                                       timeZoneCode: String,
                                       emailURI: String,
                                       phone: String,
                                       formattedAddressDescription: String = "",
                                       formattedPostalAddressDescription: String = "",
                                       firstLineDescription: String = "",
                                       secondLineDescription: String = "",
                                       thirdLineDescription: String = "",
                                       fourthLineDescription: String = "",
                                       postalFirstLineDescription: String = "",
                                       postalSecondLineDescription: String = "")

