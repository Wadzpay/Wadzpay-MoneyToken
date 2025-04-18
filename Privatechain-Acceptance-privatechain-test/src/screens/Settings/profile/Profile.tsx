import React, { useContext } from "react"
import { Link } from "react-router-dom"
import { UserContext } from "src/context/User"
import { RouteType } from "src/constants/routeTypes"
import { t } from "i18next"

const Profile: React.FC = () => {
  const { user } = useContext(UserContext)

  return (
    <>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        {user && (
          <>
            <div className="ml-4 mt-4" data-testid="merchantName">
              Email : {user.attributes.email}
            </div>
            <div className="ml-4 mt-4" data-testid="merchantPhoneNumber">
              Phone Number : {user.attributes.phone_number}
            </div>
            <div className="ml-4 mt-4" data-testid="merchantName">
              <Link
                to={RouteType.CHANGE_PASSWORD}
                role="button"
                className="btn btn-secondary wdz-btn-grey wdz-btn-lg"
                // style={{ textDecoration: "none" }}
              >
                {t("Change Password")}
              </Link>
            </div>
          </>
        )}
      </div>
    </>
  )
}

export default Profile
// import React, { useContext, useEffect } from "react"
// import { MerchantContext } from "src/context/Merchant"
// import { UserContext } from "src/context/User"
// import { RouteType } from "src/constants/routeTypes"
// import { Controller, useForm } from "react-hook-form"
// import { useNavigate } from "react-router-dom"
// import { yupResolver } from "@hookform/resolvers/yup"
// import { useTranslation } from "react-i18next"
// import "react-phone-number-input/style.css"
// import PhoneInput, {
//   isValidPhoneNumber,
//   getCountries
// } from "react-phone-number-input"
// import { MerchantDetailsForm } from "src/constants/formTypes"
// import { OnboardingContext } from "src/context/Onboarding"
// import { useValidationSchemas } from "src/constants/validationSchemas"
// import { useMerchant } from "src/api/user"
// import { countryCodes, industryTypes } from "src/constants/selectOptionTypes"

// const Profile: React.FC = () => {
//   const { merchantDetails } = useContext(MerchantContext)
//   const { user } = useContext(UserContext)
//   const { onboardingValues } = useContext(OnboardingContext)
//   const navigate = useNavigate()
//   const { t } = useTranslation()
//   const { merchantSchema } = useValidationSchemas()
//   const {
//     control,
//     register,
//     handleSubmit,
//     formState: { errors },
//     getValues
//   } = useForm<MerchantDetailsForm>({
//     resolver: yupResolver(merchantSchema)
//   })

//   const { mutate: merchantDetail, isLoading, error, isSuccess } = useMerchant()

//   const defaultCountryCode = getCountries().find(
//     (cc) => cc === onboardingValues.createAccount.country
//   )

//   useEffect(() => {
//     if (isSuccess) {
//       navigate(RouteType.HOME)
//     }
//   }, [isSuccess])

//   useEffect(() => {
//     if (merchantDetails) {
//       // console.log(merchantDetails)
//     }
//   }, [merchantDetails])

//   useEffect(() => {
//     if (user) {
//       // console.log(user)
//     }
//   }, [user])

//   const onUpdate = () => {
//     const {
//       name,
//       countryOfRegistration,
//       registrationCode,
//       primaryContactFullName,
//       primaryContactEmail,
//       companyType,
//       industryType,
//       primaryContactPhoneNumber,
//       merchantId
//     } = getValues()
//     merchantDetail({
//       name,
//       countryOfRegistration,
//       registrationCode,
//       primaryContactFullName,
//       primaryContactEmail,
//       companyType,
//       industryType,
//       primaryContactPhoneNumber,
//       merchantId
//     })
//   }

//   return (
//     <>
//       <div className="wp-form col-md-4">
//         <form onSubmit={handleSubmit(onUpdate)} role="form" noValidate>
//           {error && (
//             <div className="alert alert-danger" role="alert">
//               {error.message}
//             </div>
//           )}
//           <div className="form-group mt-4">
//             <label htmlFor="name">{t("Merchant Name")}</label>
//             <input
//               {...register("name")}
//               placeholder={t("Merchant Name")}
//               data-testid="nameInput"
//               type="text"
//               defaultValue={merchantDetails?.merchant?.name}
//               className={`form-control ${
//                 errors.name?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="nameError"
//             />
//             {errors.name?.message && (
//               <div id="nameError" className="invalid-feedback">
//                 {errors.name?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="countryOfRegistration">
//               {t("Country of Registration")}
//             </label>
//             <select
//               {...register("countryOfRegistration")}
//               defaultValue={merchantDetails?.merchant?.countryOfRegistration}
//               placeholder={t("Country of Registration")}
//               data-testid="countryOfRegistrationInput"
//               className={`form-control ${
//                 errors.countryOfRegistration?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="countryOfRegistrationError"
//             >
//               <option></option>
//               {Object.entries(countryCodes)
//                 .sort((a, b) => (a[1] > b[1] ? 1 : -1))
//                 .map((countryCode) => (
//                   <option key={countryCode[0]} value={countryCode[0]}>
//                     {t(countryCode[1])}
//                   </option>
//                 ))}
//             </select>
//             {errors.countryOfRegistration?.message && (
//               <div id="countryOfRegistrationError" className="invalid-feedback">
//                 {errors.countryOfRegistration?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="registrationCode">{t("Registration Code")}</label>
//             <input
//               {...register("registrationCode")}
//               placeholder={t("Registration Code")}
//               data-testid="registrationCodeInput"
//               type="text"
//               defaultValue={merchantDetails?.merchant?.registrationCode}
//               className={`form-control ${
//                 errors.registrationCode?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="registrationCodeError"
//             />
//             {errors.registrationCode?.message && (
//               <div id="registrationCodeError" className="invalid-feedback">
//                 {errors.registrationCode?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="primaryContactFullName">
//               {t("Primary Contact Full Name")}
//             </label>
//             <input
//               {...register("primaryContactFullName")}
//               placeholder={t("Primary Contact Full Name")}
//               data-testid="primaryContactFullNameInput"
//               type="text"
//               defaultValue={merchantDetails?.merchant?.primaryContactFullName}
//               className={`form-control ${
//                 errors.primaryContactFullName?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="primaryContactFullNameError"
//             />
//             {errors.primaryContactFullName?.message && (
//               <div
//                 id="primaryContactFullNameError"
//                 className="invalid-feedback"
//               >
//                 {errors.primaryContactFullName?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="primaryContactEmail">
//               {t("Primary Contact Email")}
//             </label>
//             <input
//               {...register("primaryContactEmail")}
//               placeholder={t("Primary Contact Email")}
//               data-testid="primaryContactEmailInput"
//               type="email"
//               defaultValue={merchantDetails?.merchant?.primaryContactEmail}
//               className={`form-control ${
//                 errors.primaryContactEmail?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="primaryContactEmailError"
//             />
//             {errors.primaryContactEmail?.message && (
//               <div id="primaryContactEmailError" className="invalid-feedback">
//                 {errors.primaryContactEmail?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="primaryContactPhoneNumber">
//               {t("Primary Contact Phone Number")}
//             </label>
//             <Controller
//               name="primaryContactPhoneNumber"
//               control={control}
//               rules={{
//                 validate: (value) => isValidPhoneNumber(value)
//               }}
//               render={({ field: { onChange, value } }) => (
//                 <PhoneInput
//                   value={merchantDetails?.merchant?.primaryContactPhoneNumber}
//                   onChange={onChange}
//                   defaultCountry={defaultCountryCode}
//                   placeholder={t("Primary Contact Phone Number")}
//                   id="primaryContactPhoneNumberError"
//                   data-testid="primaryContactPhoneNumberInput"
//                   aria-describedby="primaryContactPhoneNumberError"
//                   className={`form-control ${
//                     errors.primaryContactPhoneNumber?.message
//                       ? "is-invalid"
//                       : ""
//                   }`}
//                 />
//               )}
//             />
//             {errors.primaryContactPhoneNumber?.message && (
//               <div id="primaryContactPhoneNumber" className="invalid-feedback">
//                 {errors.primaryContactPhoneNumber?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="companyType">{t("Company Type")}</label>
//             <input
//               {...register("companyType")}
//               placeholder={t("Company Type")}
//               data-testid="companyTypeInput"
//               type="text"
//               defaultValue={merchantDetails?.merchant?.companyType}
//               className={`form-control ${
//                 errors.companyType?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="companyTypeError"
//             />
//             {errors.companyType?.message && (
//               <div id="companyTypeError" className="invalid-feedback">
//                 {errors.companyType?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="industryType">{t("Industry Type")}</label>
//             <select
//               {...register("industryType")}
//               placeholder={t("Industry Type")}
//               data-testid="industryTypeInput"
//               defaultValue={merchantDetails?.merchant?.industryType}
//               className={`form-control ${
//                 errors.industryType?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="industryTypeError"
//             >
//               <option></option>
//               {Object.entries(industryTypes).map((industryType) => (
//                 <option key={industryType[0]} value={industryType[0]}>
//                   {industryType[1]}
//                 </option>
//               ))}
//             </select>
//             {errors.industryType?.message && (
//               <div id="industryTypeError" className="invalid-feedback">
//                 {errors.industryType?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group mt-4">
//             <label htmlFor="merchantId">{t("Merchant Id")}</label>
//             <input
//               {...register("merchantId")}
//               placeholder={t("Merchant Id")}
//               data-testid="merchantIdeInput"
//               type="text"
//               defaultValue={merchantDetails?.merchant?.merchantId}
//               className={`form-control ${
//                 errors.merchantId?.message ? "is-invalid" : ""
//               }`}
//               aria-describedby="merchantIdError"
//             />
//             {errors.merchantId?.message && (
//               <div id="merchantIdError" className="invalid-feedback">
//                 {errors.merchantId?.message}
//               </div>
//             )}
//           </div>
//           <div className="form-group row row-cols-auto">
//             <div className="col mt-4">
//               <button
//                 type="submit"
//                 className="btn btn-primary wdz-btn-primary"
//                 disabled={isLoading}
//               >
//                 Update
//               </button>
//             </div>
//           </div>
//         </form>
//       </div>
//     </>
//   )
// }

// export default Profile
